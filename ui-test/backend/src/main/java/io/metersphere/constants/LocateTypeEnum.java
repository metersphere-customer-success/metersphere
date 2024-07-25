package io.metersphere.constants;

public enum LocateTypeEnum {
    //页面元素定位类型 元素对象 元素定位
    ELEMENTOBJECT("elementObject"),
    ELEMENTLOCATOR("elementLocator"),
    //元素定位的子类别 css
    ID("id"),
    NAME("name"),
    CLASSNAME("className"),
    TAGNAME("tagName"),
    LINKTEXT("linkText"),
    PARTIALLINKTEXT("partialLinkText"),
    CSS("css"),
    XPATH("xpath"),
    LABEL("label"),
    VALUE("value"),
    INDEX("index");
    private String typeName;

    LocateTypeEnum(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public static boolean contains(String type) {
        for (LocateTypeEnum locateTypeEnum : LocateTypeEnum.values()) {
            if (locateTypeEnum.typeName.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
