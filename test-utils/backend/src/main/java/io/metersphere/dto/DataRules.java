package io.metersphere.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataRules implements Serializable {
    private String id;

    private String workspaceId;

    private String ruleContext;

    private String name;

    private String testPoint;

    private String type;

    private String caseQuality;

    private Integer iter;

    private Integer genNum;

    private String genType;

    private Integer genConcurrent;

    private String encryptMethod;

    private Long createTime;

    private Long updateTime;

    private String creator;

    private String projectId;

    private String nodeId;

    private String nodePath;

    private static final long serialVersionUID = 1L;
}