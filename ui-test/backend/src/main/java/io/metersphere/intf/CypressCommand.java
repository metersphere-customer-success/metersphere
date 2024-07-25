package io.metersphere.intf;


import org.json.JSONObject;

public interface CypressCommand extends Command {
    String _toCypressScript(JSONObject param);

    String getCypressScript();
}
