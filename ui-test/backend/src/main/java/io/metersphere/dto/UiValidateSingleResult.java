package io.metersphere.dto;

import lombok.Data;

/**
 * 后端统一做指令校验的返回结果
 */

@Data
public class UiValidateSingleResult {
    /**
     * 指令 id
     */
    private String id;
    /**
     * 父亲指令 id
     */
    private String parentId;
    /**
     * 指令序号
     */
    private String index;
    /**
     * 指令名称
     */
    private String name;
    /**
     * 校验结果
     */
    private boolean result;
    /**
     * 错误信息
     */
    private String msg;
}
