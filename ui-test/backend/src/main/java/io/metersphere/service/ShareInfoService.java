package io.metersphere.service;

import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.ShareInfo;
import io.metersphere.base.mapper.ShareInfoMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioReportMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.i18n.Translator;
import io.metersphere.utils.ShareUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@Service
public class ShareInfoService {

    @Resource
    private ShareInfoMapper shareInfoMapper;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private ExtUiScenarioReportMapper extUiScenarioReportMapper;

    public void validate(String shareId, String customData) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        validateExpired(shareInfo);
        if (shareInfo == null) {
            MSException.throwException("ShareInfo not exist!");
        } else {
            if (!StringUtils.equals(customData, shareInfo.getCustomData())) {
                MSException.throwException("ShareInfo validate failure!");
            }
        }
    }

    public void validateExpired(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        this.validateExpired(shareInfo);
    }

    /**
     * 不加入事务，抛出异常不回滚
     * 若在当前类中调用请使用如下方式调用，否则该方法的事务注解不生效
     * ShareInfoService shareInfoService = CommonBeanFactory.getBean(ShareInfoService.class);
     * shareInfoService.validateExpired(shareInfo);
     *
     * @param shareInfo
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void validateExpired(ShareInfo shareInfo) {
        // 有效期根据类型从ProjectApplication中获取
        if (shareInfo == null) {
            MSException.throwException(Translator.get("connection_expired"));
        }
        String type = ProjectApplicationType.UI_SHARE_REPORT_TIME.toString();
        String projectId = "";

        ScenarioReportResultWrapper reportResult = extUiScenarioReportMapper.get(shareInfo.getCustomData());
        if (reportResult != null) {
            projectId = reportResult.getProjectId();
        }

        if (StringUtils.isBlank(type) || Strings.isBlank(projectId)) {
            millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
        } else {
            ProjectApplication projectApplication = baseProjectApplicationService.getProjectApplication(projectId, type);
            if (projectApplication.getTypeValue() == null) {
                millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
            } else {
                String expr = projectApplication.getTypeValue();
                long timeMills = ShareUtil.getTimeMills(shareInfo.getUpdateTime(), expr);
                millisCheck(System.currentTimeMillis(), timeMills, shareInfo.getId());
            }
        }
    }

    private void millisCheck(long compareMillis, long millis, String shareInfoId) {
        if (compareMillis > millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            MSException.throwException(Translator.get("connection_expired"));
        }
    }
}
