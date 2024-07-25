package io.metersphere.scenario.request;

import lombok.Data;

@Data
public class AuthConfig {
    private String verification;
    private String username;
    private String password;

}
