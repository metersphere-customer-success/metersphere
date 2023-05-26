package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FileModuleBlob implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module_blob.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{file_module_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "存储库路径")
    private String repositoryPath;

    @Schema(title = "存储库Token")
    private String repositoryUserName;

    @Schema(title = "存储库Token")
    private String repositoryToken;

    @Schema(title = "存储库描述")
    private byte[] repositoryDesc;

    private static final long serialVersionUID = 1L;
}