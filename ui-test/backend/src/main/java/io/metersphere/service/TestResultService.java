package io.metersphere.service;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.dto.UiCommandResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResultService {
    @Resource
    private UiScenarioReportService uiScenarioReportService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private UiEnvironmentService uiEnvironmentService;

    /**
     * 执行结果存储
     *
     * @param dto 执行结果
     */
    public void saveResults(ResultDTO dto) {
        // ui 结果处理
        uiScenarioReportService.saveUiResult(dto.getRequestResults(), dto);
    }

    /**
     * cookie 结果处理
     * testId -> environmentId 刷新对应环境 cookie
     *
     * @param dto 执行结果
     */
    public void processCookie(ResultDTO dto) {
        try {
            ApiTestEnvironmentWithBLOBs apiTestEnvironment = apiTestEnvironmentMapper.selectByPrimaryKey(dto.getReportId());
            if (apiTestEnvironment == null) {
                LogUtil.info("处理完cookie发现环境居然不存在了！", dto.getReportId());
                return;
            }
            List<RequestResult> queue = dto.getRequestResults();
            if (CollectionUtils.isNotEmpty(queue)) {
                String header = queue.get(queue.size() - 1).getResponseResult().getHeaders();
                LogUtil.info(String.format("获取 cookie 场景执行结束，场景 id: %s, 返回 headers: %s", dto.getTestId(), header));

                if (StringUtils.isNoneBlank(header)) {
                    List<UiCommandResult> response = JSON.parseArray(header, UiCommandResult.class);
                    response = response.stream().filter(r -> StringUtils.equalsIgnoreCase(r.getId(), "cookie")).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(response)) {
                        LogUtil.info("获取cookie失败，返回结果不存在cookie，场景id", dto.getTestId());
                        return;
                    }
                    uiEnvironmentService.updateCookie(dto.getReportId(), response.get(0).getOutputList().get(0).getValue());
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }


    public void testEnded(ResultDTO dto) {
        // 删除串行资源锁
        if (StringUtils.equals(dto.getRunType(), RunModeConstants.SERIAL.toString())) {
            redisTemplate.delete(RunModeConstants.SERIAL.name() + "_" + dto.getReportId());
        }
        if (dto.getRequestResults() == null) {
            dto.setRequestResults(new LinkedList<>());
        }
        uiScenarioReportService.testEnded(dto);
    }
}
