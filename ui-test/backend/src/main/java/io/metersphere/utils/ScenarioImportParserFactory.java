package io.metersphere.utils;

import io.metersphere.dto.parser.SideParser;
import io.metersphere.intf.ScenarioParser;
import java.util.HashMap;
import java.util.Map;

public class ScenarioImportParserFactory {

    public enum ScenarioImportPlatform {
        Metersphere, SeleniumIDE, Jmeter
    }

    private static Map<String, ScenarioParser> parserMap = new HashMap<String, ScenarioParser>() {{
        put(ScenarioImportPlatform.SeleniumIDE.name(), new SideParser());
    }};

    public static ScenarioParser getImportParser(String platform) {
        return parserMap.get(platform);
    }
}
