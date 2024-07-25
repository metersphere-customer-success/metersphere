package io.metersphere.dto.request.controller.loop;

import lombok.Data;

@Data
public class CountController {
    private String loops;
    private long interval;
    private boolean proceed;
    private Object requestResult;

}
