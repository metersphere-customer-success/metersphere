package io.metersphere.constants;

public class ViewTargetTypeConstants {
    //表示前端选择一个元素库的元素的定位
    public static final String LOCATOR = "Locator";
    //表示前端选择一个浏览器对象
    public static final String BROWSER = "Browser";
    //表示前端直接输入一个文本
    public static final String STRING = "String";
    //表示前端需要输入一个坐标 (x,y)
    public static final String COORD = "Coord";
    //表示前端需要一个窗口的句柄
    public static final String HANDLE = "Handle";
    //表示前端直接输入一个数字
    public static final String NUMBER = "Number";
    //表示前端直接输入一个矩形定义 Specify a rectangle with coordinates and lengths (e.g., "x: 257,y: 300, width: 462, height: 280")
    public static final String REGION = "Region";
    //表示一个分辨率 如  1280x800
    public static final String RESOLUTION = "Resolution";
}
