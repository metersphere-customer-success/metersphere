package io.metersphere.dto.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtractXPath extends ExtractCommon {
    public ExtractXPath() {
        setType(ExtractType.XPATH);
    }
}
