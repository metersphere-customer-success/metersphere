package io.metersphere.vo.extraction;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.LocateTypeEnum;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.utils.TemplateUtils;
import io.metersphere.utils.UiGlobalConfigUtil;
import io.metersphere.vo.BaseLocator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class UiExtractElementVO extends UiCommandBaseVo {

    private String type = UiVoType.EXTRACTELEMENT;
    private BaseLocator locator;
    // 具体提取类型
    private String extractType;
    // 元素属性名
    private String attributeName;
    // 变量名
    private String varName;
    // 变量值
    private String varValue;
    // xpath 地址
    private String xpathAddress;
    // 是否是字符串
    private boolean ifString;

    @Override
    public String getCommand(MsUiCommand command) {
        if (StringUtils.isNotBlank(extractType)) {
            if (StringUtils.equalsIgnoreCase("store", extractType)) {
                if (ifString) {
                    // 是字符串类型，对应 IDE 转成 store 指令
                    return "store";
                } else {
                    return "storeJson";
                }
            }
            return extractType;
        }
        return null;
    }

    @Override
    public String getTarget() {
        // store
        if (StringUtils.equalsIgnoreCase("store", extractType) || StringUtils.equalsIgnoreCase("//store", extractType)) {
            targetType = ifString ? "String" : "Object";
            return TemplateUtils.escapeQuotes(varValue);
        }

        // storeXpathCount
        if (StringUtils.equalsIgnoreCase("storeXpathCount", extractType)
                && StringUtils.isNotBlank(xpathAddress)) {
            if (UiGlobalConfigUtil.getConfig().isOperating()) {
                return TemplateUtils.escapeQuotes(String.format("xpath=%s", xpathAddress));
            }
            return TemplateUtils.escapeQuotes(String.format("xpath=%s", xpathAddress));
        }

        if (locator == null) {
            return "";
        }
        // storeAttribute
        if (StringUtils.equalsAnyIgnoreCase(extractType, "storeAttribute", "storeCssAttribute")) {
            if (StringUtils.isNotBlank(locator.locateType) && StringUtils.isNotBlank(locator.viewLocator)) {
                if (UiGlobalConfigUtil.getConfig().isOperating()) {
                    return String.format("%s=%s@%s", locator.locateType, locator.viewLocator, attributeName);
                }
                return TemplateUtils.escapeQuotes(String.format("%s=%s@%s", locator.locateType, locator.viewLocator, attributeName));
            } else {
                return TemplateUtils.escapeQuotes(String.format("%s@%s", locator.formatLocateString(), attributeName));
            }
        }

        // storeValue、storeText
        return locator.formatLocateString();
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        if (StringUtils.isNotBlank(varName)) {
            return varName;
        }
        return null;
    }

    private Pattern attributeLocatorRegex = Pattern.compile("^\\w+=(\\w+)@(\\w+)$");

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiExtractElementVO vo = new UiExtractElementVO();
        String originTarget = sideCommand.getTarget();
        vo.setExtractType(replaceNote(sideCommand.getCommand()));

        if ("store".equalsIgnoreCase(vo.getExtractType())) {
            vo.setVarValue(TemplateUtils.resetQuotes(originTarget));
            if (!isObject(originTarget)) {
                vo.setIfString(true);
            } else {
                vo.setIfString(false);
            }
        } else if ("storeJson".equalsIgnoreCase(vo.getExtractType())) {
            //去掉转义字符
            originTarget = TemplateUtils.resetQuotes(originTarget);
            vo.setVarValue(originTarget);
            vo.setExtractType("store");
            vo.setIfString(false);
        } else if ("storeXpathCount".equalsIgnoreCase(vo.getExtractType())) {
            if (StringUtils.isNotBlank(originTarget) && originTarget.startsWith("xpath=")) {
                String str = originTarget.substring(originTarget.indexOf("=") + 1);
                str = TemplateUtils.resetQuotes(str);
                vo.setXpathAddress(str);
            } else {
                originTarget = TemplateUtils.resetQuotes(originTarget);
                vo.setXpathAddress(originTarget);
            }
        }
        if (StringUtils.equalsAnyIgnoreCase(vo.getExtractType(), "storeAttribute", "storeCssAttribute")) {
            BaseLocator locator = new BaseLocator();
            locator.setElementType("elementLocator");
            String target = sideCommand.getTarget();
            if (target.contains("=")) {
                String type = target.split("=")[0];
                if (LocateTypeEnum.contains(type)) {
                    locator.setLocateType(type);
                }
            }else {
                locator.setLocateType(LocateTypeEnum.XPATH.getTypeName());
            }
            if (StringUtils.isNotBlank(target) && target.indexOf("@") != -1 && target.indexOf("=") != -1) {
                locator.setViewLocator(target.substring(target.indexOf("=") + 1, target.lastIndexOf("@")));
                vo.setAttributeName(target.substring(target.lastIndexOf("@") + 1));
            }
            vo.setLocator(locator);
        } else {
            vo.setLocator(BaseLocator.parse(originTarget));
        }
        Matcher m = attributeLocatorRegex.matcher(originTarget);
        if (m.find()) {
            vo.setAttributeName(m.group(2));
            if (vo.getLocator() != null) {
                vo.getLocator().setViewLocator(m.group(1));
            }
        }
        vo.setVarName(sideCommand.getValue());
        msUiCommand.setVo(vo);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiExtractElementVO vo = null;
        if (command.getVo() instanceof UiExtractElementVO) {
            vo = (UiExtractElementVO) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        if (StringUtils.isBlank(vo.getExtractType())) {
            return getFailResult(command, Translator.get("extract_type") + Translator.get("is_null"));
        }
        if ("store".equalsIgnoreCase(vo.getExtractType()) || "storeJson".equalsIgnoreCase(vo.getExtractType())) {
            if (StringUtils.isAnyBlank(vo.getVarName(), vo.getVarValue())) {
                return getFailResult(command, Translator.get("varname_or_value") + Translator.get("is_null"));
            }
        } else if ("storeText".equalsIgnoreCase(vo.getExtractType())) {
            if (StringUtils.isAnyBlank(vo.getVarName())) {
                return getFailResult(command, Translator.get("varname") + Translator.get("is_null"));
            }
            String validateStr = vo.getLocator().validateLocateString();
            if (StringUtils.isNotBlank(validateStr)) {
                return getFailResult(command, Translator.get("locator_is_null"));
            }
        } else if ("storeXpathCount".equalsIgnoreCase(vo.getExtractType())) {
            if (StringUtils.isAnyBlank(vo.getVarName())) {
                return getFailResult(command, Translator.get("varname") + Translator.get("is_null"));
            }
            if (StringUtils.isBlank(vo.getXpathAddress())) {
                return getFailResult(command, Translator.get("xpath") + Translator.get("is_null"));
            }
        } else if ("storeAttribute".equalsIgnoreCase(vo.getExtractType())) {
            if (StringUtils.isAnyBlank(vo.getVarName())) {
                return getFailResult(command, Translator.get("varname") + Translator.get("is_null"));
            }
            String validateStr = vo.getLocator().validateLocateString();
            if (StringUtils.isNotBlank(validateStr)) {
                return getFailResult(command, Translator.get("locator_is_null"));
            }
            if (StringUtils.isBlank(attributeName)) {
                return getFailResult(command, Translator.get("attributeName") + Translator.get("is_null"));
            }
        }
        return r;
    }
}
