package io.metersphere.dto.track;

import io.metersphere.base.domain.TestCaseReview;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseReviewDTO extends TestCaseReview {

    private String projectName;
    private String reviewerName;
    private String creatorName;
}
