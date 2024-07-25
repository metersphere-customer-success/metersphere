package io.metersphere.dto.testcase;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.dto.CustomFieldResourceDTO;
import io.metersphere.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseBatchRequest extends TestCaseWithBLOBs {
    private List<String> ids;
    private List<OrderRequest> orders;
    private String projectId;
    private CustomFieldResourceDTO customField;
    private QueryTestCaseRequest condition;
    private String customTemplateFieldId;
    private List<String> tagList;
    private boolean appendTag = false;

    @Getter
    @Setter
    public static class CustomFiledRequest {
        private String id;
        private String name;
        private Object value;
    }
}
