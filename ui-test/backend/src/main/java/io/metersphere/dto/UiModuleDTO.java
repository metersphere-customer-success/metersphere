package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiModuleDTO extends TreeNodeDTO<UiModuleDTO> {
    private String protocol;
}
