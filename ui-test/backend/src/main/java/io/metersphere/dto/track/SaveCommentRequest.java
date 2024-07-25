package io.metersphere.dto.track;

import io.metersphere.base.domain.TestCaseComment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveCommentRequest extends TestCaseComment {
     private String reviewId;

}
