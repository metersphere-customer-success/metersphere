package io.metersphere.constants;

/**
 * 系统级别通用的常量和枚举
 */
public final class SystemConstants {

    public static final String JSENGINE_GRAAL = "graal.js";

    public enum TestTypeEnum {
        API,
        UI,
        PERFORMANCE;
    }

    /**
     * 数据来源，此标记是为了区分拆分前与拆分后的历史报告
     * 0-拆分前的数据
     * 1-新数据
     */
    public enum DataOriginEnum {
        OLD(0),
        NEW(1);
        private int value;

        DataOriginEnum(int value) {
            this.value = value;
        }

        public Integer value() {
            return this.value;
        }
    }

    /**
     * 运行模式
     * <p>
     * local-本地调试
     * report-生成报告
     * server-后端调试
     */
    public enum UiRunModeEnum {
        LOCAL("local"),
        REPORT("report"),
        SERVER("server");
        private String name;

        UiRunModeEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * UI_MODULE-本地调试
     * report-生成报告
     * server-后端调试
     */
    public enum UIRequestOriginatorEnum {
        UI_MODULE("UI_MODULE"),
        TEST_PLAN("TEST_PLAN");
        private String name;

        UIRequestOriginatorEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }


}
