package io.metersphere.dto.track;

import io.metersphere.base.domain.TestCaseComment;
import lombok.Data;

@Data
public class TestCaseCommentDTO extends TestCaseComment {
    private String authorName;
}
