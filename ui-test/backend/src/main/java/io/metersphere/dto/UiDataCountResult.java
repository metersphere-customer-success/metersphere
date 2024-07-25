package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * API数据统计查询结果类
 */
@Getter
@Setter
public class UiDataCountResult {
    //分组统计字段
    private String groupField;
    //数据统计
    private long countNumber;
}
