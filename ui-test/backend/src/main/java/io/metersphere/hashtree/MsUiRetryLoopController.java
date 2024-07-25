package io.metersphere.hashtree;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.TemplateConstants;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.TemplateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsUiRetryLoopController extends MsRetryLoopController {
    private String type = "RetryUiLoopController";
    private String clazzName = MsUiRetryLoopController.class.getCanonicalName();

    private long retryNum;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;

        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        httpRequest(tree);
        HashTree ifController = ifController(tree);
        HashTree groupTree = controller(ifController);
        appendBeanShellPrePocessor(groupTree, TemplateConstants.FUNCTION_INIT_RETRY_SCRIPT, config);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.toHashTree(groupTree, el.getHashTree(), config);
            });
        }
        appendBeanShell(groupTree, TemplateConstants.FUNCTION_RETRY_SCRIPT, config);
    }

    private WhileController initWhileController(String condition) {
        if (StringUtils.isEmpty(condition)) {
            return null;
        }
        WhileController controller = new WhileController();
        controller.setEnabled(this.isEnable());
        controller.setName("WhileController");
        controller.setProperty(TestElement.TEST_CLASS, WhileController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("WhileControllerGui"));
        controller.setCondition(condition);
        return controller;
    }

    private String script(String scriptName, ParameterConfig config) {
        Map<String, Object> param = new HashMap<>();
        param.put("maxRetryTimes", retryNum);
        if (CollectionUtils.isNotEmpty(config.getHeaders())) {
            param.put("scenarioName", config.getHeaders().get(0).getValue());
        }
        String script = ((Map<String, String>) CommonBeanFactory.getBean("uiCommonFunctionMap")).get(scriptName);
        script = TemplateUtils.replaceVars(script, param);
        return script;
    }

    private HashTree controller(HashTree tree) {
        String whileCondition = "${__javaScript(\"${retry}\" != \"stop\")}";
        HashTree hashTree = tree.add(initWhileController(whileCondition));
        return hashTree;
    }

    private HashTree ifController(HashTree tree) {
        String ifCondition = "${JMeterThread.last_sample_ok}";
        IfController controller = new IfController();
        controller.setEnabled(this.isEnable());
        controller.setName("IfController");
        controller.setProperty(TestElement.TEST_CLASS, IfController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("IfControllerPanel"));
        controller.setCondition(ifCondition);
        HashTree hashTree = tree.add(controller);
        return hashTree;
    }

    private HashTree httpRequest(HashTree tree) {
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(this.isEnable());
        sampler.setName("HTTP grid");
        if (StringUtils.isEmpty(this.getName())) {
            sampler.setName("HTTPSamplerProxy");
        }

        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));
        sampler.setMethod("POST");
        sampler.setContentEncoding("UTF-8");
        sampler.setFollowRedirects(true);
        sampler.setUseKeepAlive(true);
        SystemParameter seleniumSP = CommonBeanFactory.getBean(SystemParameterMapper.class).selectByPrimaryKey(ParamConstants.BASE.SELENIUM_DOCKER_URL.getValue());
        sampler.setPath(getGridPath(Optional.ofNullable(seleniumSP).orElse(defaultGridConfig()).getParamValue()));
        sampler.addNonEncodedArgument("", "{\"operationName\":\"\",\"variables\":{},\"query\":\"query Summary {\\n  grid {\\n    uri\\n    totalSlots\\n    nodeCount\\n    maxSession\\n    sessionCount\\n    sessionQueueSize\\n    version\\n    __typename\\n  }\\n}\"}", "");
        sampler.setPostBodyRaw(true);
        return tree.add(sampler);
    }

    private String getGridPath(String paramValue) {
        if (paramValue.endsWith("/")) {
            return StringUtils.join(paramValue, "graphql");
        } else {
            return StringUtils.join(paramValue, "/graphql");
        }
    }

    private SystemParameter defaultGridConfig() {
        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setParamValue("http://127.0.0.1:4444");
        return systemParameter;
    }

    /**
     * 后置 beanshell
     *
     * @param tree
     * @param scriptName
     */
    private void appendBeanShell(HashTree tree, String scriptName, ParameterConfig config) {
        // 添加超时处理，防止死循环
        JSR223PostProcessor postProcessor = new JSR223PostProcessor();
        postProcessor.setName(scriptName);
        postProcessor.setProperty(TestElement.TEST_CLASS, JSR223Sampler.class.getName());
        postProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        postProcessor.setProperty("scriptLanguage", "beanshell");
        postProcessor.setProperty("script", script(scriptName, config));
        tree.add(postProcessor);
    }

    /**
     * 前置 beanshell
     *
     * @param tree
     * @param scriptName
     */
    private void appendBeanShellPrePocessor(HashTree tree, String scriptName, ParameterConfig config) {
        // 添加超时处理，防止死循环
        BeanShellPreProcessor preProcessor = new BeanShellPreProcessor();
        preProcessor.setName(scriptName);
        preProcessor.setProperty(TestElement.TEST_CLASS, BeanShellPreProcessor.class.getName());
        preProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        preProcessor.setProperty("scriptLanguage", "beanshell");
        preProcessor.setProperty("script", script(scriptName, config));
        preProcessor.setProperty("resetInterpreter", false);
        tree.add(preProcessor);
    }
}
