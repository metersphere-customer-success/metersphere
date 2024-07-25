package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class UiCheckRefResp {

    private String result;

    private List<UiCheckRefDTO> data;
}
