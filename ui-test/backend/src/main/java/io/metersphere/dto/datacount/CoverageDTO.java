package io.metersphere.dto.datacount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoverageDTO {
    public String rateOfCoverage = "0%";
    public long coverate = 0;
    public long notCoverate = 0;
}
