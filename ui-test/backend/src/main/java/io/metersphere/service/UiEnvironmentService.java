package io.metersphere.service;


import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.config.HttpConfigCondition;
import io.metersphere.dto.EnvironmentConfig;
import io.metersphere.dto.ScenarioVariable;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.utils.MiniTimingUtil;
import io.metersphere.utils.TemplateUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UiEnvironmentService {
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    private final String HTTP_CONFIG = "httpConfig";
    private final String COOKIE = "cookie";

    /**
     * 刷新 cookie
     *
     * @param environmentId
     * @param cookie
     */
    public void updateCookie(String environmentId, String cookie) {
        if (StringUtils.isAnyBlank(environmentId, cookie)) {
            return;
        }
        ApiTestEnvironmentWithBLOBs apiTestEnvironment = apiTestEnvironmentMapper.selectByPrimaryKey(environmentId);
        if (apiTestEnvironment == null) {
            return;
        }
        String configStr = apiTestEnvironment.getConfig();
        if (StringUtils.isBlank(configStr)) {
            return;
        }
        JSONObject configJSON = new JSONObject(configStr);
        if (configJSON.has(HTTP_CONFIG)) {
            JSONObject httpJSON = configJSON.optJSONObject(HTTP_CONFIG);
            if (httpJSON != null && httpJSON.has(COOKIE)) {
                JSONArray cookieJSONArray = httpJSON.optJSONArray(COOKIE);
                if (cookieJSONArray.length() > 0) {
                    JSONObject cookieJSON = cookieJSONArray.optJSONObject(0);
                    if (cookieJSON == null) {
                        cookieJSON = new JSONObject();
                        cookieJSON.put("enable", true);
                        cookieJSON.put("id", UUID.randomUUID().toString());
                        cookieJSON.put("relevanceId", environmentId);
                        cookieJSON.put("expireTime", "1D");
                    }
                    cookieJSON.put("cookie", cookie);
                    cookieJSON.put("updateTime", System.currentTimeMillis());
                }
            }
        }
        apiTestEnvironment.setConfig(configJSON.toString());
        apiTestEnvironmentMapper.updateByPrimaryKey(apiTestEnvironment);
    }

    public boolean cookieExpired(String environmentId) {
        if (StringUtils.isBlank(environmentId)) {
            return false;
        }
        ApiTestEnvironmentWithBLOBs apiTestEnvironment = apiTestEnvironmentMapper.selectByPrimaryKey(environmentId);
        if (apiTestEnvironment == null) {
            return false;
        }
        String configStr = apiTestEnvironment.getConfig();
        if (StringUtils.isBlank(configStr)) {
            return false;
        }
        JSONObject configJSON = new JSONObject(configStr);

        if (configJSON.has(HTTP_CONFIG)) {
            JSONObject httpJSON = configJSON.optJSONObject(HTTP_CONFIG);
            if (httpJSON != null && httpJSON.has(COOKIE)) {
                JSONArray cookieJSONArray = httpJSON.optJSONArray(COOKIE);
                if (cookieJSONArray.length() > 0) {
                    JSONObject cookieJSON = cookieJSONArray.optJSONObject(0);
                    if (cookieJSON != null && cookieJSON.optBoolean("enable")) {
                        if (cookieJSON.has("expireTime") && cookieJSON.has("updateTime")) {
                            String expireTimeStr = cookieJSON.optString("expireTime");
                            Long expireTime = MiniTimingUtil.getTimeFromStr(expireTimeStr);
                            Long updateTime = cookieJSON.optLong("updateTime");
                            if (System.currentTimeMillis() - updateTime >= expireTime) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public String getCookieConfig(String environmentId, String keyName) {
        if (StringUtils.isBlank(environmentId)) {
            return "";
        }
        ApiTestEnvironmentWithBLOBs apiTestEnvironment = apiTestEnvironmentMapper.selectByPrimaryKey(environmentId);
        if (apiTestEnvironment == null) {
            return "";
        }
        String configStr = apiTestEnvironment.getConfig();
        if (StringUtils.isBlank(configStr)) {
            return "";
        }
        JSONObject configJSON = new JSONObject(configStr);

        if (configJSON.has(HTTP_CONFIG)) {
            JSONObject httpJSON = configJSON.optJSONObject(HTTP_CONFIG);
            if (httpJSON != null && httpJSON.has(COOKIE)) {
                JSONArray cookieJSONArray = httpJSON.optJSONArray(COOKIE);
                if (cookieJSONArray.length() > 0) {
                    JSONObject cookieJSON = cookieJSONArray.optJSONObject(0);
                    if (cookieJSON != null) {
                        if (cookieJSON.has(keyName)) {
                            return cookieJSON.optString(keyName);
                        }
                    }
                }
            }
        }
        return "";
    }

    /**
     * 替换域名里面的变量引用，域名里面的变量引用不受 api，ui 的使用场景的限制，都可以引用
     *
     * @return
     */
    public EnvironmentConfig replaceDomainVars(EnvironmentConfig originalConfig) {
        if (originalConfig == null || originalConfig.getCommonConfig() == null || CollectionUtils.isEmpty(originalConfig.getCommonConfig().getVariables())) {
            return originalConfig;
        }
        List<ScenarioVariable> variableList = originalConfig.getCommonConfig().getVariables().stream().filter(s -> StringUtils.equalsAnyIgnoreCase(s.getType(), "STRING", "CONSTANT")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(variableList)) {
            return originalConfig;
        }
        Map<String, String> paramMap = new HashMap<>();
        try {
            variableList.forEach(v -> {
                paramMap.put(v.getName(), String.valueOf(v.getValue()));
            });
        } catch (Exception e) {
            LogUtil.error("在replaceDomainVars的时候转换paramMap失败", e);
        }
        List<HttpConfigCondition> conditions = originalConfig.getHttpConfig().getConditions();
        if (conditions == null) {
            return originalConfig;
        }
        conditions.forEach(c -> {
            c.setSocket(TemplateUtils.replaceVars(c.getSocket(), paramMap));
            c.setDomain(TemplateUtils.replaceVars(c.getDomain(), paramMap));
        });
        return originalConfig;
    }


    /**
     * 查询环境配置信息
     *
     * @param environmentId
     * @return
     */
    public EnvironmentConfig convertCnfToClazz(String environmentId) {
        if (StringUtils.isBlank(environmentId)) {
            return null;
        }
        ApiTestEnvironmentWithBLOBs env = CommonBeanFactory.getBean(BaseEnvironmentService.class).get(environmentId);
        String cnfStr = env.getConfig();
        try {
            JSONObject jsonObject = new JSONObject(cnfStr);
            if (jsonObject.has("commonConfig")) {
                JSONObject commonConfig = jsonObject.optJSONObject("commonConfig");
                if (commonConfig.has("variables")) {
                    JSONArray jsonArray = commonConfig.optJSONArray("variables");
                    JSONArray legalJsonArray = new JSONArray();
                    Iterator<Object> iterator = jsonArray.iterator();
                    while (iterator.hasNext()) {
                        JSONObject obj = (JSONObject) iterator.next();
                        if (!obj.optBoolean("enable")) {
                            continue;
                        }
                        String scope = obj.optString("scope");
                        if (StringUtils.isBlank(scope) || StringUtils.equalsIgnoreCase(scope, "ui") || StringUtils.equalsIgnoreCase(scope, "api") && StringUtils.equalsIgnoreCase(obj.optString("type"), "CONSTANT")) {
                            legalJsonArray.put(obj);
                        }
                    }
                    commonConfig.put("variables", legalJsonArray);
                }
                if (commonConfig.has("hosts")){
                    commonConfig.remove("hosts");
                }
                if (commonConfig.has("enableHost")){
                    commonConfig.remove("enableHost");
                }
            }
            cnfStr = jsonObject.toString();
        } catch (Exception e) {
            LogUtil.error("转换env时出错！", e);
        }
        return JSON.parseObject(cnfStr, EnvironmentConfig.class);
    }

}
