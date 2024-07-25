package io.metersphere.constants;

/**
 * 指令的分类
 */
public enum AtomicCommandTypeEnum {

    /**
     * DOM 元素操作
     */
    elementOperation("elementOperation", "元素操作", "元素操作"),
    /**
     * 校验 包括 assert 和 verify
     * assert 校验失败会终止当前测试并退出返回结果
     * verify 校验失败不会终止当前测试
     */
    validation("validation", "校验", "校驗"),
    /**
     * 对话框操作
     * 包括 HTML 原生的 prompt，alert
     */
    dialogOperation("dialogOperation", "对话框操作", "對話框操作"),
    /**
     * 鼠标操作 单击元素,双击元素，单机某个坐标，按下左键（未弹起）
     */
    mouseOperation("mouseOperation", "鼠标操作", "鼠標操作"),
    /**
     * 浏览器操作 打开 URL,选择 iFrame，选择 window，设置窗口大小
     */
    browserOperation("browserOperation", "浏览器操作", "瀏覽器操作"),
    /**
     * 键盘输入命令
     */
    inputOperation("inputOperation", "键盘操作", "鍵盤操作"),
    /**
     * 编程控制操作 if else
     */
    programController("programController", "流程控制", "流程控制"),
    /**
     * 数据提取
     */
    dataExtraction("dataExtraction", "数据提取", "數據提取");

    //英文名称
    private String name;
    private String cnName;
    private String twName;

    public String getName() {
        return name;
    }

    public String getCnName() {
        return cnName;
    }

    public String getTwName() {
        return twName;
    }

    AtomicCommandTypeEnum(String name, String cnName, String twName) {
        this.name = name;
        this.cnName = cnName;
        this.twName = twName;
    }

    public static AtomicCommandTypeEnum getByName(String name) {
        for (AtomicCommandTypeEnum type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public static AtomicCommandTypeEnum getByCNName(String cnName) {
        for (AtomicCommandTypeEnum type : values()) {
            if (type.cnName.equalsIgnoreCase(cnName)) {
                return type;
            }
        }
        return null;
    }

    public static AtomicCommandTypeEnum getByTWName(String twName) {
        for (AtomicCommandTypeEnum type : values()) {
            if (type.twName.equalsIgnoreCase(twName)) {
                return type;
            }
        }
        return null;
    }
}
