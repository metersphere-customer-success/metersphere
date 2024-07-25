package io.metersphere.intf;

import io.metersphere.dto.UiTestImportRequest;
import java.io.InputStream;

public interface ScenarioParser<T> {
    T parse(InputStream source, UiTestImportRequest request);
}
