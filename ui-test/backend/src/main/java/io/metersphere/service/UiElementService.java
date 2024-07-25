package io.metersphere.service;

import com.alibaba.excel.EasyExcelFactory;
import com.google.common.collect.Lists;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.UiElementMapper;
import io.metersphere.base.mapper.ext.BaseProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.base.mapper.ext.ExtUiElementMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.*;
import io.metersphere.dto.element.UiElementDto;
import io.metersphere.dto.excel.FunctionUiTemplateWriteHandler;
import io.metersphere.dto.excel.UiElementExcelData;
import io.metersphere.dto.excel.UiElementExcelDataFactory;
import io.metersphere.dto.excel.UiElementExcelDataListener;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.AutomationReference;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.utils.ServiceUtil;
import io.metersphere.utils.TemplateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiElementService {

    @Resource
    UiElementMapper uiElementMapper;
    @Resource
    ExtUiElementMapper extUiElementMapper;
    @Resource
    private BaseProjectVersionMapper extProjectVersionMapper;
    @Resource
    private UiElementReferenceService uiElementReferenceService;
    @Resource
    private ExtModuleNodeMapper extModuleNodeMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public String add(UiElement uiElement) {
        checkExist(uiElement);
        uiElement.setId(UUID.randomUUID().toString());
        uiElement.setNum(ServiceUtils.getNextNum(uiElement.getProjectId(), UiElement.class, extUiElementMapper::getMaxNumByProjectId));
        uiElement.setOrder(ServiceUtils.getNextOrder(uiElement.getProjectId(), extUiElementMapper::getLastOrder));
        //直接点保存 || 复制走的逻辑
        if (StringUtils.isAllBlank(uiElement.getRefId(), uiElement.getVersionId())) {
            //新创建测试用例，默认使用最新版本
            uiElement.setRefId(uiElement.getId());
            uiElement.setVersionId(extProjectVersionMapper.getDefaultVersion(uiElement.getProjectId()));
        } else if (StringUtils.isBlank(uiElement.getRefId()) && StringUtils.isNotBlank(uiElement.getVersionId())) {
            //从版本选择直接创建
            uiElement.setRefId(uiElement.getId());
        }
        //完全新增一条记录直接就是最新
        uiElement.setLatest(true);
        uiElement.setCreateTime(System.currentTimeMillis());
        uiElement.setUpdateTime(System.currentTimeMillis());
        uiElement.setCreateUser(SessionUtils.getUserId());
        uiElement.setUpdateUser(SessionUtils.getUserId());
        uiElementMapper.insert(uiElement);
        return uiElement.getId();
    }


    public List<UiElementDto> list(BaseQueryRequest request) {
        initRequest(request);
        List<UiElement> list = extUiElementMapper.list(request);
        return buildUserInfo(list);
    }

    public List<UiElement> listNames(BaseQueryRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrderByField(request.getOrders(), "create_time"));
        return extUiElementMapper.listNames(request);
    }

    public List<UiElementDto> buildUserInfo(List<UiElement> list) {
        List<UiElementDto> uiElementDtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return uiElementDtoList;
        }
        Set<String> userIds = new HashSet<>();
        list.forEach(i -> {
            userIds.add(i.getCreateUser());
            if (StringUtils.isNotBlank(i.getUpdateUser())) {
                userIds.add(i.getUpdateUser());
            }
        });
        Map<String, String> idModulePathMap = getIdModulePathMap(list.get(0).getProjectId());
        if (CollectionUtils.isNotEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(new ArrayList<>(userIds));
            list.forEach(caseResult -> {
                caseResult.setCreateUser(userMap.get(caseResult.getCreateUser()));
                if (StringUtils.isBlank(caseResult.getUpdateUser())) {
                    caseResult.setUpdateUser(caseResult.getCreateUser());
                } else {
                    caseResult.setUpdateUser(userMap.get(caseResult.getUpdateUser()));
                }
                UiElementDto uiElementDto = new UiElementDto();
                BeanUtils.copyBean(uiElementDto,caseResult);
                uiElementDto.setModulePath(idModulePathMap.get(uiElementDto.getModuleId()));
                uiElementDtoList.add(uiElementDto);
            });
        }
        return uiElementDtoList;
    }

    private Map<String, String> getIdModulePathMap(String projectId) {
        List<ModuleNodeDTO> moduleList = extModuleNodeMapper.getNodeTreeByProjectIdOrderLevel("ui_element_module", projectId);
        //所有模块的ID 及其全路径的map
        Map<String, String> idPathMap = new HashMap<>();
        String initParentModulePath = "/root";
        Map<String, String> parentModulePathMap = new HashMap<>();
        parentModulePathMap.put("root", initParentModulePath);
        buildPath(moduleList,idPathMap,parentModulePathMap);
        return idPathMap;
    }

    private void buildPath(List<ModuleNodeDTO> nodeTreeByProjectId, Map<String, String> idPathMap, Map<String, String> parentModulePathMap) {
        //当前层级的模块的所有子模块的集合
        Map<String, List<ModuleNodeDTO>> idChildrenMap = new HashMap<>();
        int i = 0;
        for (ModuleNodeDTO apiModuleDTO : nodeTreeByProjectId) {
            if (StringUtils.isBlank(apiModuleDTO.getParentId()) || StringUtils.equals(apiModuleDTO.getParentId(), "0")) {
                apiModuleDTO.setParentId("root");
            }
            String parentModulePath = parentModulePathMap.get(apiModuleDTO.getParentId());
            if (parentModulePath != null) {
                if (parentModulePath.equals("/root")) {
                    apiModuleDTO.setPath("/" + apiModuleDTO.getName());
                } else {
                    apiModuleDTO.setPath(parentModulePath + "/" + apiModuleDTO.getName());
                }
            } else {
                apiModuleDTO.setPath("/" + apiModuleDTO.getName());
            }
            idPathMap.put(apiModuleDTO.getId(), apiModuleDTO.getPath());

            i = i + 1;
            List<ModuleNodeDTO> childrenList = idChildrenMap.get(apiModuleDTO.getId());
            if (apiModuleDTO.getChildren() != null) {
                if (childrenList != null) {
                    childrenList.addAll(apiModuleDTO.getChildren());
                } else {
                    idChildrenMap.put(apiModuleDTO.getId(), apiModuleDTO.getChildren());
                }
            }
            parentModulePathMap.put(apiModuleDTO.getId(), apiModuleDTO.getPath());
        }
        if (i == nodeTreeByProjectId.size() && nodeTreeByProjectId.size() > 0) {
            Collection<List<ModuleNodeDTO>> values = idChildrenMap.values();
            List<ModuleNodeDTO> childrenList = new ArrayList<>();
            for (List<ModuleNodeDTO> value : values) {
                childrenList.addAll(value);
            }
            buildPath(childrenList, idPathMap, parentModulePathMap);
        }
    }


    public UiElement get(String id) {
        return uiElementMapper.selectByPrimaryKey(id);
    }

    public int delete(String id) {
        //删除引用关系
        uiElementReferenceService.refreshReference(Lists.newArrayList(id));
        return uiElementMapper.deleteByPrimaryKey(id);
    }

    public int edit(UiElement uiElement) {
        checkExist(uiElement);
        //获取原来的信息
        UiElement originElement = uiElementMapper.selectByPrimaryKey(uiElement.getId());

        uiElement.setUpdateTime(System.currentTimeMillis());
        // 暂时不考虑版本 todo
        uiElement.setVersionId(null);
        uiElement.setCreateUser(null);
        uiElement.setCreateTime(null);
        uiElement.setUpdateUser(SessionUtils.getUserId());
        int result = uiElementMapper.updateByPrimaryKeySelective(uiElement);

        //模块改变了才进行修改
        if (originElement != null && !originElement.getModuleId().equals(uiElement.getModuleId())) {
            //更新元素引用关系
            uiElementReferenceService.updateReference(uiElement.getProjectId(), uiElement.getId(), uiElement.getModuleId());
        }

        return result;
    }

    private void checkExist(UiElement uiElement) {
        if (uiElement.getName() != null) {
            UiElementExample example = new UiElementExample();
            UiElementExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(uiElement.getName());
            criteria.andProjectIdEqualTo(uiElement.getProjectId());
            criteria.andModuleIdEqualTo(uiElement.getModuleId());
            if (StringUtils.isNotBlank(uiElement.getId())) {
                criteria.andIdNotEqualTo(uiElement.getId());
            }
            if (uiElementMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("ui_element_already_exists_data") + uiElement.getName());
            }
        }
    }

    public void deleteByNodeIds(List<String> nodeIds) {
        if (CollectionUtils.isNotEmpty(nodeIds)) {
            UiElementExample example = new UiElementExample();
            example.createCriteria().andModuleIdIn(nodeIds);

            List<UiElement> uiElements = uiElementMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(uiElements)) {
                //删除引用关系
                uiElementReferenceService.refreshReference(uiElements.stream().map(UiElement::getId).collect(Collectors.toList()));
            }

            uiElementMapper.deleteByExample(example);
        }
    }

    /**
     * 初始化部分参数
     *
     * @param request 请求
     */
    private void initRequest(BaseQueryRequest request) {
        request.setName(TemplateUtils.escapeSqlSpecialChars(request.getName()));
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
    }

    public List<UiElement> listByExample(UiElementExample example) {
        return uiElementMapper.selectByExample(example);
    }

    /**
     * 元素导入模板下载
     */
    public void templateExport(Integer importType, HttpServletResponse response) {
        UiElementExcelData excelDataLocal = new UiElementExcelDataFactory().getExcelDataLocal();
        List<List<String>> headList = excelDataLocal.getHead(importType == 1);
        EasyExcelExporter easyExcelExporter = new EasyExcelExporter(excelDataLocal.getClass());
        FunctionUiTemplateWriteHandler handler = new FunctionUiTemplateWriteHandler();
        easyExcelExporter.exportByCustomWriteHandler(response, headList, generateExportDatas(importType == 1),
                Translator.get("ui_element_import_template_name"), Translator.get("ui_element_import_template_sheet"), handler);
    }

    private List<List<Object>> generateExportDatas(boolean needNum) {
        List<List<Object>> list = Lists.newArrayList();
        StringBuilder path = new StringBuilder();
//            SessionUser user = SessionUtils.getUser();
        List<String> typeList = Lists.newArrayList("id", "name", "className", "index", "tagName", "linkText", "partialLinkText", "css", "xpath", "label", "value");
        for (int i = 1; i <= 5; i++) {
            List<Object> rowData = new ArrayList<>();
            if (needNum) {
                rowData.add("");
            }
            rowData.add(Translator.get("ui_element_name") + i);
            path.append("/").append(Translator.get("module")).append(i);
            rowData.add(path.toString());
            rowData.add(typeList.get(i - 1));
            rowData.add(Translator.get("ui_element_locator"));
            rowData.add(Translator.get("ui_element_description"));
            list.add(rowData);
        }
        return list;
    }

    public ExcelResponse importFile(MultipartFile multipartFile, UiElementImportRequest request) {
        if (multipartFile == null) {
            MSException.throwException(Translator.get("upload_fail"));
        }
        String userId = SessionUtils.getUserId();

        List<ExcelErrData<UiElementExcelData>> errList = new ArrayList<>();
        boolean isUpdated = false;

        try {
            //根据本地语言环境选择用哪种数据对象进行存放读取的数据
            Class clazz = new UiElementExcelDataFactory().getExcelDataByLocal();
            UiElementExcelDataListener easyExcelListener = new UiElementExcelDataListener(clazz, userId, request.getProjectId());
            //读取excel数据
            EasyExcelFactory.read(multipartFile.getInputStream(), easyExcelListener).sheet().doRead();
            errList = easyExcelListener.getErrList();

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }

        return getImportResponse(errList, isUpdated);
    }

    private ExcelResponse getImportResponse(List<ExcelErrData<UiElementExcelData>> errList, boolean isUpdated) {
        ExcelResponse excelResponse = new ExcelResponse();
        excelResponse.setIsUpdated(isUpdated);
        //如果包含错误信息就导出错误信息
        if (!errList.isEmpty()) {
            excelResponse.setSuccess(false);
            excelResponse.setErrList(errList);
        } else {
            excelResponse.setSuccess(true);
        }
        return excelResponse;
    }

    public void elementExport(HttpServletResponse response, BaseQueryRequest request) {
        try {
            UiElementExcelData excelDataLocal = new UiElementExcelDataFactory().getExcelDataLocal();
            if (BooleanUtils.isTrue(request.isSelectAll())) {
                request.setIds(new ArrayList<>());
            }
            List<UiElementDto> uiElements = list(request);
            List<UiElementExcelData> list = uiElements.parallelStream().map(v -> {
                UiElementExcelData data = new UiElementExcelData();
                data.setCustomNum(String.valueOf(v.getNum()));
                data.setNodePath(v.getModulePath());
                data.setName(v.getName());
                data.setElementLocator(v.getLocation());
                data.setLocatorType(v.getLocationType());
                data.setDescription(v.getDescription());
                return data;
            }).collect(Collectors.toList());

            boolean importFileNeedNum = true;
            List<List<String>> headList = excelDataLocal.getHead(importFileNeedNum);
            List<List<Object>> testCaseDataByExcelList = this.generateElementExcel(headList, list);
            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(excelDataLocal.getClass());
            easyExcelExporter.exportByCustomWriteHandler(response, headList, testCaseDataByExcelList,
                    Translator.get("ui_element_import_template_name"), Translator.get("ui_element_import_template_sheet"));


        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }
    }

    private List<List<Object>> generateElementExcel(List<List<String>> headListParams, List<UiElementExcelData> datas) {
        List<List<Object>> returnDatas = new ArrayList<>();
        //转化excel头
        List<String> headList = new ArrayList<>();
        for (List<String> list : headListParams) {
            headList.addAll(list);
        }

        for (UiElementExcelData model : datas) {
            List<Object> list = new ArrayList<>();

            for (String head : headList) {
                if (StringUtils.equalsAnyIgnoreCase(head, "ID")) {
                    list.add(model.getCustomNum());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Name", "元素名稱", "元素名称")) {
                    list.add(model.getName());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Module", "所屬模塊", "所属模块")) {
                    list.add(model.getNodePath());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "LocatorType", "定位類型", "定位类型")) {
                    list.add(model.getLocatorType());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "ElementLocator", "元素定位", "元素定位")) {
                    list.add(model.getElementLocator());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Description", "描述", "描述")) {
                    list.add(model.getDescription());
                }
            }
            returnDatas.add(list);
        }

        return returnDatas;
    }

    public int delete(BaseQueryRequest batchVO) {
        ServiceUtil.getSelectAllIds(batchVO, batchVO,
                (query) -> extUiElementMapper.listIdsByQuery(query));
        int count = 0;
        for (String id : batchVO.getIds()) {
            count += delete(id);
        }
        return count;
    }

    public int batchEdit(BaseQueryRequest batchVO) {
        ServiceUtil.getSelectAllIds(batchVO, batchVO,
                (query) -> extUiElementMapper.listIdsByQuery(query));
        int count = 0;
        //检测模块下名称是否存在
        if(CollectionUtils.isEmpty(batchVO.getIds())){
            return count;
        }

        UiElementExample example = new UiElementExample();
        example.createCriteria().andIdIn(batchVO.getIds());
        List<UiElement> uiElements = uiElementMapper.selectByExample(example);
        Map<String, UiElement> cacheMap = uiElements.stream().collect(Collectors.toConcurrentMap(UiElement::getId, v -> v));
        for (String id : batchVO.getIds()) {
            UiElement cacheElement = cacheMap.get(id);
            if (Objects.isNull(cacheElement)) {
                continue;
            }
            UiElement editVO = new UiElement();
            BeanUtils.copyBean(editVO, batchVO);
            editVO.setId(id);
            editVO.setName(cacheElement.getName());
            checkExist(editVO);
        }

        for (String id : batchVO.getIds()) {
            UiElement editVO = new UiElement();
            BeanUtils.copyBean(editVO, batchVO);
            editVO.setId(id);
            editVO.setName(null);
            count += edit(editVO);
        }
        return count;
    }

    public UIElementReferenceResult referenceBatch(String projectId, BaseQueryRequest request) {
        ServiceUtil.getSelectAllIds(request, request, (baseQueryRequest -> extUiElementMapper.listIdsByQuery(request)));
        return uiElementReferenceService.reference(projectId, request.getIds());
    }

    private int getNextNum(String projectId) {
        UiElement uiElement = extUiElementMapper.getNextNum(projectId);
        if (uiElement == null || uiElement.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(uiElement.getNum() + 1).orElse(100001);
        }
    }

    public void batchCopy(UiElementBatchRequest request) {

        ServiceUtil.getSelectAllIds(request, request,
                (query) -> extUiElementMapper.listIdsByQuery(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        UiElementExample e = new UiElementExample();
        e.createCriteria().andIdIn(ids);
        List<UiElement> uiElements = uiElementMapper.selectByExample(e);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        Long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extUiElementMapper::getLastOrder);
        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < uiElements.size(); i++) {
                UiElement element = uiElements.get(i);
                element.setId(UUID.randomUUID().toString());
                element.setName(ServiceUtils.getCopyName(element.getName()));
                element.setModuleId(request.getModuleId());
                element.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                element.setCreateTime(System.currentTimeMillis());
                element.setUpdateTime(System.currentTimeMillis());
                element.setRefId(element.getId());
                element.setNum(nextNum++);
                //重置UserId
                element.setCreateUser(SessionUtils.getUserId());
                uiElementMapper.insert(element);
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public String getLogDetails(String id) {
        UiElement bloBs = uiElementMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, AutomationReference.automationColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            UiElementExample example = new UiElementExample();
            example.createCriteria().andIdIn(ids);
            example.setOrderByClause("create_time DESC ");
            List<UiElement> uiElements = uiElementMapper.selectByExample(example);
            List<String> names = uiElements.stream().map(UiElement::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), uiElements.get(0).getProjectId(), String.join(",", names), uiElements.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }
}
