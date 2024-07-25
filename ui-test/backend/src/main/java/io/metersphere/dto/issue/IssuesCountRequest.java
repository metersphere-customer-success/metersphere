package io.metersphere.dto.issue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuesCountRequest {
    private String workspaceId;
    private String creator;
}
