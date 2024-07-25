package io.metersphere.intf;


import org.json.JSONObject;

public interface SeleniumIDECommand extends Command {
    String _toWebdriverSamplerScript(JSONObject param);

    String getWebdriverScript();
}
