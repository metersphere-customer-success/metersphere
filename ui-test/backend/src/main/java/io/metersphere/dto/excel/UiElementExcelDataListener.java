package io.metersphere.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.metersphere.base.domain.UiElement;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.excel.annotation.NotRequired;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.utils.ExcelValidateHelper;
import io.metersphere.i18n.Translator;
import io.metersphere.service.UiElementModuleService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class UiElementExcelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    private static final Integer IMPORT_UPDATE = 1;

    private String projectId;

    private Class excelDataClass;
    Set<String> customIds = Sets.newConcurrentHashSet();
    private UiElementModuleService uiElementModuleService;

    protected String userId;
    protected Integer importType;

    protected List<ExcelErrData<UiElementExcelData>> errList = new ArrayList<>();

    protected final List<UiElementExcelData> excelDataList = new ArrayList<>();

    private Map<Integer, String> headMap;
    private Map<String, String> excelHeadToFieldNameDic = new HashMap<>();

    public List<ExcelErrData<UiElementExcelData>> getErrList() {
        return errList;
    }

    List<String> typeList = Lists.newArrayList("id", "name", "className", "index", "tagName", "tag", "linkText", "partialLinkText", "css", "xpath", "label", "value");

    /**
     * 每隔2000条存储数据库，然后清理list ，方便内存回收
     */
    protected static final int BATCH_COUNT = 2000;

    protected List<UiElementExcelData> list = new ArrayList<>();

    public UiElementExcelDataListener(Class clazz, String userId, String projectId) {
        this.excelDataClass = clazz;
        this.projectId = projectId;
        this.userId = userId;
        this.importType = IMPORT_UPDATE;
        this.uiElementModuleService = CommonBeanFactory.getBean(UiElementModuleService.class);
    }


    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {
        String errMsg;
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        String updateMsg = "update_element";
        UiElementExcelData uiElementExcelData = this.parseDataToModel(data);
        try {
            //根据excel数据实体中的jakarta.validation + 正则表达式来校验excel数据
            errMsg = ExcelValidateHelper.validateEntity(uiElementExcelData);
            //自定义校验规则
            errMsg = validate(uiElementExcelData, errMsg);
        } catch (NoSuchFieldException e) {
            errMsg = Translator.get("parse_data_error");
            LogUtil.error(e.getMessage(), e);
        }

        if (!StringUtils.isEmpty(errMsg)) {
            //如果errMsg只有"update"，说明待更新
            if (!errMsg.equals(updateMsg)) {
                ExcelErrData excelErrData = new ExcelErrData(uiElementExcelData, rowIndex,
                        Translator.get("number") + " " + rowIndex + " " + Translator.get("row") + Translator.get("error")
                                + "：" + errMsg);
                errList.add(excelErrData);
            }
        } else {
            list.add(uiElementExcelData);
        }
        if (list.size() > BATCH_COUNT) {
            saveData();
            list.clear();
        }
    }

    private String validate(UiElementExcelData data, String errMsg) {
        StringBuilder stringBuilder = new StringBuilder(errMsg);
        String customId = data.getCustomNum();
        if (StringUtils.isEmpty(customId)) {
            //stringBuilder.append(Translator.get("id_required")).append(";");
        } else if (customIds.contains(customId.toLowerCase())) {
            stringBuilder.append(Translator.get("id_repeat_in_table")).append(";");
        } else {
            customIds.add(customId.toLowerCase());
        }

        String nodePath = data.getNodePath();
        //校验”所属模块"
        if (nodePath != null) {
            String[] nodes = nodePath.split("/");
            //模块名不能为空
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    stringBuilder.append(Translator.get("module_not_null")).append("; ");
                    break;
                }
            }
            //增加字数校验，每一层不能超过100字
            for (int i = 0; i < nodes.length; i++) {
                String nodeStr = nodes[i];
                if (StringUtils.isNotEmpty(nodeStr)) {
                    if (nodeStr.trim().length() > 100) {
                        stringBuilder.append(Translator.get("module")).append(Translator.get("test_track.length_less_than")).append("100:").append(nodeStr);
                        break;
                    }
                }
            }
        }

                /*
        校验Excel中是否有ID
            有的话校验ID是否已在当前项目中存在，存在则更新用例，
            不存在则继续校验看是否重复，不重复则新建用例。
         */
        //检测id
        int customNumId = -1;
        try {
            customNumId = Integer.parseInt(data.getCustomNum());
        } catch (Exception e) {
        }
        if (StringUtils.isNotBlank(data.getCustomNum()) && customNumId < 0) {
            stringBuilder.append(Translator.get("id_not_rightful")).append("[").append(data.getCustomNum()).append("]; ");
        }

        if (StringUtils.isNotBlank(data.getLocatorType())) {
            List<String> list = typeList.stream().filter(v -> StringUtils.equalsAnyIgnoreCase(data.getLocatorType(), v)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                stringBuilder.append(Translator.get("ui_element_locator_type"))
                        .append(" [").append(data.getLocatorType()).append("] ").append(Translator.get("user_import_format_wrong"));
            }
        }

        // @Data 重写了 equals 和 hashCode 方法
        boolean excelExist = this.excelDataList.contains(data);

        if (excelExist) {
            // excel exist
            stringBuilder.append(Translator.get("ui_element_already_exists_excel") + "：" + data.getName() + "; ");
        } else {
            //校验模块下元素名是否存在
            boolean exist = uiElementModuleService.checkElementExist(data, projectId);
            if (exist) {
                stringBuilder.append(Translator.get("module")).append(Translator.get("ui_element_already_exists_data"));
            } else {
                this.excelDataList.add(data);
            }
        }
        return stringBuilder.toString();

    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        try {
            this.genExcelHeadToFieldNameDicAndGetNotRequiredFields();
        } catch (NoSuchFieldException e) {
            LogUtil.error(e);
        }
        this.formatHeadMap();
        super.invokeHeadMap(headMap, context);
    }

    private void formatHeadMap() {
        for (Integer key : headMap.keySet()) {
            String name = headMap.get(key);
            if (excelHeadToFieldNameDic.containsKey(name)) {
                headMap.put(key, excelHeadToFieldNameDic.get(name));
            }
        }
    }


    private UiElementExcelData parseDataToModel(Map<Integer, String> data) {

        UiElementExcelData excelDataLocal = new UiElementExcelDataFactory().getExcelDataLocal();
        for (Map.Entry<Integer, String> headEntry : headMap.entrySet()) {
            Integer index = headEntry.getKey();
            String field = headEntry.getValue();
            String value = StringUtils.isEmpty(data.get(index)) ? "" : data.get(index);

            if (StringUtils.equals(field, "customNum")) {
                excelDataLocal.setCustomNum(value);
            } else if (StringUtils.equals(field, "name")) {
                excelDataLocal.setName(value);
            } else if (StringUtils.equals(field, "nodePath")) {
                excelDataLocal.setNodePath(value);
            } else if (StringUtils.equals(field, "locatorType")) {
                excelDataLocal.setLocatorType(value);
            } else if (StringUtils.equals(field, "elementLocator")) {
                excelDataLocal.setElementLocator(value);
            } else if (StringUtils.equals(field, "description")) {
                if (StringUtils.isNotBlank(value)) {
                    if (value.length() > 512) {
                        excelDataLocal.setDescription(value.substring(0, 512));
                    } else {
                        excelDataLocal.setDescription(value);
                    }
                }
            }

        }

        return excelDataLocal;
    }

    public Set<String> genExcelHeadToFieldNameDicAndGetNotRequiredFields() throws NoSuchFieldException {

        Set<String> result = new HashSet<>();
        Field field;
        Field[] fields = excelDataClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            field = excelDataClass.getDeclaredField(fields[i].getName());
            field.setAccessible(true);
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                StringBuilder value = new StringBuilder();
                for (String v : excelProperty.value()) {
                    value.append(v);
                }
                excelHeadToFieldNameDic.put(value.toString(), field.getName());
                // 检查是否必有的头部信息
                if (field.getAnnotation(NotRequired.class) != null) {
                    result.add(value.toString());
                }
            }
        }
        return result;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        list.clear();
    }

    private void saveData() {
        if (!errList.isEmpty()) {
            return;
        }

        //找到数据库中存在的cumNum
        List<UiElement> result = uiElementModuleService.findExistElementNum(projectId, list.parallelStream().filter(v -> StringUtils.isNotBlank(v.getCustomNum())).map(v -> Integer.parseInt(v.getCustomNum())).collect(Collectors.toList()));
        List<Integer> numList = result.parallelStream().map(UiElement::getNum).distinct().collect(Collectors.toList());
        //新增
        List<UiElementExcelData> toBeInsert = list.parallelStream().filter(v -> StringUtils.isBlank(v.getCustomNum()) || !numList.contains(Integer.parseInt(v.getCustomNum()))).collect(Collectors.toList());
        uiElementModuleService.handleImportSave(projectId, toBeInsert, "");

        //更新
        Map<Integer, List<UiElement>> map = result.stream().collect(Collectors.groupingBy(UiElement::getNum));
        List<UiElementExcelData> toBeUpdate = list.parallelStream().filter(v -> StringUtils.isNotBlank(v.getCustomNum()) && numList.contains(Integer.parseInt(v.getCustomNum()))).collect(Collectors.toList());
        uiElementModuleService.handleImportUpdate(projectId, toBeUpdate, "", map);

    }
}
