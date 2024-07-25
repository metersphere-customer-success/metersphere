package io.metersphere.vo;

import io.metersphere.base.domain.UiElement;
import io.metersphere.base.mapper.UiElementMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.LocateTypeEnum;
import io.metersphere.i18n.Translator;
import io.metersphere.utils.TemplateUtils;
import io.metersphere.utils.UiGlobalConfigUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ui 基础定位类
 */
@Data
public class BaseLocator {
    //定位方式 elementObject 元素对象 elementLocator
    public String elementType;
    //模块id
    public String moduleId;
    //元素id
    public String elementId;
    //定位方式
    public String locateType;
    //元素定位
    public String viewLocator;
    //坐标
    public List<Coord> coords;

    /**
     * 格式化定位器
     *
     * @return
     */
    public String formatLocateString() {
        String locator = getLocateString();
        if (UiGlobalConfigUtil.getConfig().isOperating()) {
            //导出不用转义双引号
            return locator;
        }
        return TemplateUtils.escapeQuotes(locator);
    }

    private String getLocateString() {
        String locator = "";
        if (StringUtils.equalsIgnoreCase("elementObject", elementType)) {
            UiElement uiElement = CommonBeanFactory.getBean(UiElementMapper.class).selectByPrimaryKey(elementId);
            if (uiElement != null) {
                locator = uiElement.getLocationType() + "=" + uiElement.getLocation();
            }
        } else {
            if (StringUtils.isNotBlank(locateType) && StringUtils.isNotBlank(viewLocator)) {
                locator = locateType + "=" + viewLocator;
            }
        }
        return locator;
    }

    /**
     * 格式化定位器
     *
     * @return
     */
    public String formatLocateStringWithOutEscape() {
        String locator = getLocateString();
        if (UiGlobalConfigUtil.getConfig().isOperating()) {
            //导出不用转义双引号
            return locator;
        }
        return locator;
    }

    /**
     * 元素定位校验
     *
     * @return
     */
    public String validateLocateString() {
        if (StringUtils.equalsIgnoreCase("elementObject", elementType)) {
            UiElement uiElement = CommonBeanFactory.getBean(UiElementMapper.class).selectByPrimaryKey(elementId);
            if (uiElement == null) {
                return Translator.get("element_is_null");
            }
        } else {
            if (StringUtils.isBlank(locateType) || StringUtils.isBlank(viewLocator)) {
                return Translator.get("locator_is_null");
            }
        }
        return "";
    }

    /**
     * 格式化定位器
     *
     * @return
     */
    public static BaseLocator parse(String locatorString) {
        try {
            BaseLocator locator = new BaseLocator();
            locator.setElementType("elementLocator");
            //如果不是标准的格式 先统一认为是 xpath
            locator.setLocateType(LocateTypeEnum.XPATH.getTypeName());
            locator.setViewLocator(locatorString);
            if (locatorString.contains("=")) {
                int index = locatorString.indexOf("=");
                String type = locatorString.split("=")[0];
                if (LocateTypeEnum.contains(type)) {
                    locator.setLocateType(locatorString.split("=")[0]);
                    locator.setViewLocator(locatorString.substring(index + 1));
                }
            }
            return locator;
        } catch (Exception e) {
            return new BaseLocator();
        }
    }

    /**
     * 坐标类
     */
    @Data
    public static class Coord {
        private String type = "Coord";
        //索引
        public String index;
        //横坐标
        public int x;
        //纵坐标
        public int y;

        public Coord() {

        }

        public Coord(String index, int x, int y) {
            this.type = type;
            this.index = index;
            this.x = x;
            this.y = y;
        }

        public Coord(String type, String index, int x, int y) {
            this.type = type;
            new Coord(index, x, y);
        }
    }

    public List<Coord> getCoords() {
        if (CollectionUtils.isEmpty(this.coords)) {
            return new ArrayList<>() {{
                add(new Coord(UUID.randomUUID().toString(), 0, 0));
            }};
        }
        return coords;
    }
}
