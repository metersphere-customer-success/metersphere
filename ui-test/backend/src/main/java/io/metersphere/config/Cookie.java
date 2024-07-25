package io.metersphere.config;

import lombok.Data;

@Data
public class Cookie {
    private String cookie;
    private String expireTime;
    private long updateTime;
    private String relevanceId;
    private boolean enable;
}
