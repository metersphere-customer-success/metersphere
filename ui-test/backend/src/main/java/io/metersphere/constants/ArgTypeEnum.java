package io.metersphere.constants;

import io.metersphere.intf.ParamProcessor;
import io.metersphere.intf.Validator;
import io.metersphere.utils.processor.Processor;
import org.apache.commons.lang3.StringUtils;


/**
 * 参数的类型的定义以及其校验器
 */
public enum ArgTypeEnum {
    alertText("alertText", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    answer("answer", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    attributeLocator("attributeLocator", ViewTargetTypeConstants.LOCATOR, validateString(), Processor.LocatorProcessor),
    arrayVariableName("arrayVariableName", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    conditionalExpression("conditionalExpression", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    coord("coord", ViewTargetTypeConstants.COORD, validateString(), Processor.StringProcessor),
    expectedValue("expectedValue", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    expression("expression", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    formLocator("formLocator", ViewTargetTypeConstants.LOCATOR, validateString(), Processor.LocatorProcessor),
    handle("handle", ViewTargetTypeConstants.HANDLE, validateString(), Processor.LocatorProcessor),
    // todo 类型的配置
    iteratorVariableName("iteratorVariableName", ViewTargetTypeConstants.STRING, validateString(), Processor.LocatorProcessor),
    json("json", ViewTargetTypeConstants.STRING, validateString(), Processor.LocatorProcessor),
    keySequence("keySequence", ViewTargetTypeConstants.STRING, validateString(), Processor.LocatorProcessor),
    locator("locator", ViewTargetTypeConstants.LOCATOR, validateString(), Processor.LocatorProcessor),
    locatorOfDragDestinationObject("locatorOfDragDestinationObject", ViewTargetTypeConstants.LOCATOR, validateString(), Processor.LocatorProcessor),
    locatorOfObjectToBeDragged("locatorOfObjectToBeDragged", ViewTargetTypeConstants.LOCATOR, validateString(), Processor.LocatorProcessor),
    loopLimit("loopLimit", ViewTargetTypeConstants.NUMBER, validateString(), Processor.StringProcessor),
    message("message", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    optionLocator("optionLocator", ViewTargetTypeConstants.LOCATOR, validateString(), Processor.LocatorProcessor),
    optionalFlag("optionalFlag", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    pattern("pattern", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    region("region", ViewTargetTypeConstants.REGION, validateString(), Processor.StringProcessor),
    resolution("resolution", ViewTargetTypeConstants.RESOLUTION, validateString(), Processor.StringProcessor),
    script("script", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    selectLocator("selectLocator", ViewTargetTypeConstants.LOCATOR, validateString(), Processor.LocatorProcessor),
    testCase("testCase", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    text("text", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    times("times", ViewTargetTypeConstants.NUMBER, validateString(), Processor.StringProcessor),
    url("url", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    value("value", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    variableName("variableName", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor),
    waitTime("waitTime", ViewTargetTypeConstants.NUMBER, validateString(), Processor.StringProcessor),
    xpath("xpath", ViewTargetTypeConstants.STRING, validateString(), Processor.StringProcessor);
    private String type;
    private String viewType;
    private ParamProcessor paramProcessor;
    private Validator validator;
    private String jsValidator;

    ArgTypeEnum(String type, String msType, Validator validator) {
        this.type = type;
        this.viewType = msType;
        this.validator = validator;
    }

    ArgTypeEnum(String type, String msType, Validator validator, ParamProcessor paramProcessor) {
        this.type = type;
        this.viewType = msType;
        this.validator = validator;
        this.paramProcessor = paramProcessor;
    }


    public Validator getValidator() {
        return validator;
    }

    public ParamProcessor getParamProcessor() {
        return paramProcessor;
    }

    public String getJsValidator() {
        return jsValidator;
    }

    public String getType() {
        return type;
    }

    public String getMsType() {
        return viewType;
    }

    private static Validator validateString() {
        return param -> StringUtils.isNotEmpty(param);
    }

    @Override
    public String toString() {
        return "ArgTypeEnum{" +
                "type='" + type + '\'' +
                ", msType='" + viewType + '\'' +
                '}';
    }
}

