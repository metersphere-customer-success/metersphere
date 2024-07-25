package io.metersphere.constants;

public class ValidateTypeConstants {

    /**
     * 断言值的类型
     */
    public enum ValidateValueEnum {
        EQUAL("equal"),
        NOT_EQUAL("notEqual"),
        CONTAIN("contain"),
        NOT_CONTAIN("notContain"),
        GREATER("greater"),
        GREATER_EQUAL("greaterEqual"),
        LOWER("lower"),
        LOWER_EQUAL("lowerEqual"),
        NULL("null"),
        NOT_NULL("notNull"),
        ;
        private String type;

        public String getType() {
            return type;
        }

        ValidateValueEnum(String type) {
            this.type = type;
        }
    }

    /**
     * 断言元素的类型
     */
    public enum ValidateElementEnum {
        ASSERT_IN_TEXT("assertInText"),
        ASSERT_NOT_IN_TEXT("assertNotInText"),
        ;
        private String type;

        public String getType() {
            return type;
        }

        ValidateElementEnum(String type) {
            this.type = type;
        }
    }
}
