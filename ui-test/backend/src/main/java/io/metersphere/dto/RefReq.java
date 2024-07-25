package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RefReq {
    private String scenarioId;
    private String scenarioType;
    private List<String> workspace;
    private List<String> project;
    private Integer timeOrder;
    private Integer idOrder;
}
