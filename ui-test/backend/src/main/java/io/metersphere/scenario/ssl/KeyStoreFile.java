package io.metersphere.scenario.ssl;

import io.metersphere.request.BodyFile;
import lombok.Data;

@Data
public class KeyStoreFile {
    private String id;
    private String name;
    private String type;
    private String updateTime;
    private String password;
    private BodyFile file;

}
