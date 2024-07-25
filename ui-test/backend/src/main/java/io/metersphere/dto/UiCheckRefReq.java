package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UiCheckRefReq {
    private List<String> scenarioIds;
}
