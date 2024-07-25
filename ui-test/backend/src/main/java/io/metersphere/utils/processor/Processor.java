package io.metersphere.utils.processor;


import io.metersphere.base.domain.UiElement;
import io.metersphere.base.mapper.UiElementMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.LocateTypeEnum;
import io.metersphere.intf.ParamProcessor;
import io.metersphere.vo.UiAtomicCommandVO;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

public class Processor {
    private static final String PARAM_TYPE_TARGET = "target";
    private static final String PARAM_TYPE_VALUE = "value";

    public static ParamProcessor StringProcessor = (command, uiCommand, type) -> {
        if (PARAM_TYPE_TARGET.equalsIgnoreCase(type)) {
            UiAtomicCommandVO targetVO = uiCommand.getTargetVO();
            if (targetVO == null) {
                //第一次导入，程序设置用于展示的值
                targetVO = new UiAtomicCommandVO();
                targetVO.setText(uiCommand.getTarget());
                uiCommand.setTargetVO(targetVO);
                return;
            }
            if (targetVO != null) {
                uiCommand.setTarget(Optional.ofNullable(targetVO.getText()).orElse("").toString());
            }
        }
        if (PARAM_TYPE_VALUE.equalsIgnoreCase(type)) {
            UiAtomicCommandVO valueVO = uiCommand.getValueVO();
            if (valueVO == null) {
                //第一次导入，程序设置用于展示的值
                valueVO = new UiAtomicCommandVO();
                valueVO.setText(uiCommand.getValue());
                uiCommand.setValueVO(valueVO);
                return;
            }
            if (valueVO != null) {
                uiCommand.setValue(Optional.ofNullable(valueVO.getText()).orElse("").toString());
            }
        }
    };

    public static ParamProcessor LocatorProcessor = (command, uiCommand, type) -> {
        if (PARAM_TYPE_TARGET.equalsIgnoreCase(type)) {
            UiAtomicCommandVO targetVO = uiCommand.getTargetVO();
            if (targetVO == null && StringUtils.isNotBlank(uiCommand.getTarget())) {
                //第一次导入，没有在前端编辑，校验直接通过
                targetVO = new UiAtomicCommandVO();
                targetVO.setElementType(LocateTypeEnum.ELEMENTLOCATOR.getTypeName());
                if (uiCommand.getTarget().contains("=")) {
                    targetVO.setLocateType(uiCommand.getTarget().split("=")[0]);
                    targetVO.setViewLocator(uiCommand.getTarget().substring(uiCommand.getTarget().indexOf("=") + 1));
                } else {
                    targetVO.setLocateType(LocateTypeEnum.ID.getTypeName());
                    targetVO.setViewLocator("");
                }
                uiCommand.setTargetVO(targetVO);
                return;
            }
            if (targetVO != null) {
                if (StringUtils.isNotBlank(targetVO.getElementType())) {
                    if (StringUtils.equalsIgnoreCase(LocateTypeEnum.ELEMENTOBJECT.getTypeName(), targetVO.getElementType())) {
                        String elementId = targetVO.getElementId();
                        UiElement uiElement = CommonBeanFactory.getBean(UiElementMapper.class).selectByPrimaryKey(elementId);
                        if (uiElement != null) {
                            uiCommand.setTarget(uiElement.getLocationType() + "=" + uiElement.getLocation());
                        }
                    } else {
                        if (StringUtils.isNotBlank(targetVO.getLocateType()) && StringUtils.isNotBlank(targetVO.getViewLocator())) {
                            uiCommand.setTarget(targetVO.getLocateType() + "=" + targetVO.getViewLocator());
                        }
                    }
                }
            }
        }
        if (PARAM_TYPE_VALUE.equalsIgnoreCase(type)) {
            UiAtomicCommandVO valueVO = uiCommand.getValueVO();
            if (valueVO == null && StringUtils.isNotBlank(uiCommand.getValue())) {
                //第一次导入，没有在前端编辑，校验直接通过
                valueVO = new UiAtomicCommandVO();
                valueVO.setElementType(LocateTypeEnum.ELEMENTLOCATOR.getTypeName());
                if (uiCommand.getValue().contains("=")) {
                    valueVO.setLocateType(uiCommand.getValue().split("=")[0]);
                    valueVO.setViewLocator(uiCommand.getValue().substring(uiCommand.getValue().indexOf("=") + 1));
                } else {
                    valueVO.setLocateType(LocateTypeEnum.ID.getTypeName());
                    valueVO.setViewLocator("");
                }

                uiCommand.setValueVO(valueVO);
                return;
            }
            if (StringUtils.isNotBlank(valueVO.getElementType())) {
                if (StringUtils.equalsIgnoreCase(LocateTypeEnum.ELEMENTOBJECT.getTypeName(), valueVO.getElementType())) {
                    String elementId = valueVO.getElementId();
                    UiElement uiElement = CommonBeanFactory.getBean(UiElementMapper.class).selectByPrimaryKey(elementId);
                    if (uiElement != null) {
                        uiCommand.setValue(uiElement.getLocationType() + "=" + uiElement.getLocation());
                    }
                } else {
                    if (StringUtils.isNotBlank(valueVO.getLocateType()) && StringUtils.isNotBlank(valueVO.getViewLocator())) {
                        uiCommand.setValue(valueVO.getLocateType() + "=" + valueVO.getViewLocator());
                    }
                }
            }
        }
    };
}
