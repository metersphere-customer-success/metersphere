package io.metersphere.validate;

import io.metersphere.dto.CustomFieldDao;
import io.metersphere.exception.CustomFieldValidateException;

public class CustomFieldTextValidator extends AbstractCustomFieldValidator {

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
    }
}
