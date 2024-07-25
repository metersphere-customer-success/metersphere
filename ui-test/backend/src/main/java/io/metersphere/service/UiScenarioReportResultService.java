package io.metersphere.service;

import io.metersphere.base.domain.UiScenarioReportResultWithBLOBs;
import io.metersphere.base.mapper.UiScenarioReportResultMapper;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.ResultConversionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioReportResultService {
    @Resource
    private UiScenarioReportResultMapper uiScenarioReportResultMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void save(String reportId, List<RequestResult> queue) {
        if (CollectionUtils.isNotEmpty(queue)) {
            if (queue.size() == 1) {
                queue.forEach(item -> {
                    // 事物控制器出来的结果特殊处理
                    if (StringUtils.isNotEmpty(item.getName()) && item.getName().startsWith("Transaction=") && CollectionUtils.isEmpty(item.getSubRequestResults())) {
                        LoggerUtil.debug("合并事物请求暂不入库");
                    } else {
                        uiScenarioReportResultMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(reportId, item));
                    }
                });
            } else {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                UiScenarioReportResultMapper batchMapper = sqlSession.getMapper(UiScenarioReportResultMapper.class);
                queue.forEach(item -> {
                    if (StringUtils.isEmpty(item.getName()) || !item.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(item.getSubRequestResults())) {
                        batchMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(reportId, item));
                    }
                });
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    public void batchSave(List<ResultDTO> dtos) {
        if (CollectionUtils.isNotEmpty(dtos)) {
            if (dtos.size() == 1 && CollectionUtils.isNotEmpty(dtos.get(0).getRequestResults()) && dtos.get(0).getRequestResults().size() == 1) {
                // 单条储存
                RequestResult requestResult = dtos.get(0).getRequestResults().get(0);
                if (StringUtils.isEmpty(requestResult.getName()) || !requestResult.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(requestResult.getSubRequestResults())) {
                    uiScenarioReportResultMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(dtos.get(0).getReportId(), requestResult));
                }
            } else {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                UiScenarioReportResultMapper batchMapper = sqlSession.getMapper(UiScenarioReportResultMapper.class);
                for (ResultDTO dto : dtos) {
                    if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                        dto.getRequestResults().forEach(item -> {
                            if (StringUtils.isEmpty(item.getName()) || !item.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(item.getSubRequestResults())) {
                                batchMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(dto.getReportId(), item));
                            }
                        });
                    }
                }
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    public UiScenarioReportResultWithBLOBs newScenarioReportResult(String reportId, String resourceId) {
        return ResultConversionUtil.newScenarioReportResult(reportId, resourceId);
    }
}
