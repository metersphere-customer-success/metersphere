package io.metersphere.vo.program.controller;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class UiIfVo extends UiProgramBaseVO {

    @Getter
    @Setter
    public static class Condition {
        private String name;
        private String value;
        private String rangeType = "value_eq";
        private String valueType;
        private String description;
    }

    private String type = UiVoType.IF;

    protected String model = "condition";
    protected String expression;
    protected String paramsFilterType;
    protected List<Condition> conditions;


    @Override
    public String getCommand(MsUiCommand command) {
        return "if";
    }

    @Override
    public String getTarget() {
        if (model.equals("condition")) {
            return parseToExpression().replaceAll("\"", "'");
        } else {
            if (expression != null) {
                return expression.replaceAll("\"", "'");
            }
            return "";
        }
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }

    protected String parseToExpression() {
        StringBuilder result = new StringBuilder();
        String spit = " && ";
        if (StringUtils.isNotBlank(paramsFilterType) && paramsFilterType.equalsIgnoreCase("OR")) {
            spit = " || ";
        }
        for (Condition condition : conditions) {
            if (!StringUtils.isAnyBlank(condition.getRangeType(), condition.getName(), condition.getValue())) {
                result.append(spit);
                result.append(parseCondition(condition));
            }
        }

        if (result.length() > 0) {
            result.delete(0, spit.length());
        }

        return result.toString();
    }

    protected static String parseCondition(Condition condition) {
        String rangeType = condition.getRangeType();
        String originValue = condition.getValue();
        String name = stringWrap(condition.getName());
       // String value = stringWrap(condition.getValue());
        Object value = stringWrapWithType(condition.getValue(), condition.getValueType());
        switch (rangeType) {
            case "value_not_eq":
                return name + " !== " + value;
            case "value_contain":
                return name + ".indexOf(" + value + ") > -1";
            case "length_eq":
                return name + ".length === " + originValue;
            case "length_not_eq":
                return name + ".length !== " + originValue;
            case "length_large_than":
                return name + ".length > " + originValue;
            case "length_shot_than":
                return name + ".length < " + originValue;
            case "regular_match":
                return name + ".search(" + value + ")";
            default:
                return name + " === " + value;
        }
    }

    protected static String stringWrap(String s) {
        if (s.startsWith("${") && s.endsWith("}")) {
            return s;
        } else {
            return "'" + s + "'";
        }
    }

    private static Object stringWrapWithType(String value, String type) {
        //如果是空的type，则是不明确的类型的值，输入什么，返回什么
        if (StringUtils.isBlank(type)) {
            return value;
        }
        Object val = value;
        try {
            switch (type) {
                case "JSON":
                case "JSONObject":
                    if (StringUtils.isBlank(value)) {
                        value = "{}";
                    }
                    val = JSON.parseObject(value);
                    break;
                case "ARRAY":
                case "JSONArray":
                    if (StringUtils.isBlank(value)) {
                        value = "[]";
                    }
                    val = JSON.parseArray(value);
                    break;

                case "NUMBER":
                    if (StringUtils.isBlank(value)) {
                        val = 0;
                    }
                   break;

                case "CONSTANT":
                case "STRING":
                    if (value.startsWith("${") && value.endsWith("}")) {
                        val = value;
                    } else {
                        val =   "'" + value + "'";
                    }
                    break;
                default:
                    val = value;
            }
        } catch (Exception e) {
            LogUtil.error("转换UI变量时报错！", e);
            MSException.throwException("转换UI变量时报错，请检查变量的格式！" + e.getMessage());
        }
        return val;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiIfVo vo = new UiIfVo();
        String originTarget = sideCommand.getTarget();
        vo.setModel("expression");
        vo.setParamsFilterType("And");
        vo.setExpression(originTarget);
        vo.setConditions(new ArrayList<>());
        msUiCommand.setVo(vo);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_COMBINATION);
        addAppendPauseCommand(msUiCommand);
        msUiCommand.setEndCommand("end");
        msUiCommand.setViewType("programController");
        return msUiCommand;
    }


    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiIfVo vo = null;
        if (command.getVo() instanceof UiIfVo) {
            vo = (UiIfVo) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if ("expression".equalsIgnoreCase(vo.getModel()) && StringUtils.isBlank(vo.getExpression())) {
            return getFailResult(command, Translator.get("expression") + Translator.get("is_null"));
        }else{

        }
        // todo 复杂条件列表的校验
        return r;
    }

}

