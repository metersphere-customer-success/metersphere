package io.metersphere.dto.track;

import lombok.Data;

@Data
public class Errors {

    private String errorType;
    private String errorNumber;
    private String percentOfErrors;
    private String percentOfAllSamples;

}
