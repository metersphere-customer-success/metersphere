import org.json.*;

boolean success = true;
try {
    String uiResponseHeaders = ctx.getPreviousResult().getResponseHeaders();

    if (uiResponseHeaders == null) {
        success = false;
    } else {
        try {
            JSONArray responseArray = new JSONArray(uiResponseHeaders);
            //检查状态 主流程失败 或者 断言有失败，步骤就失败
            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject oneResult = responseArray.optJSONObject(i);
                if (oneResult.has("processType") && oneResult.optString("processType").equalsIgnoreCase("MAIN")) {
                    if (oneResult.has("success") && !oneResult.optBoolean("success")) {
                        success = false;
                        break;
                    }
                }

                if (oneResult.has("cmdName") && oneResult.optString("cmdName").contains("assert") || oneResult.optString("cmdName").contains("verify")) {
                    if (oneResult.has("success") && !oneResult.optBoolean("success")) {
                        success = false;
                        break;
                    }
                }
            }

        } catch (Throwable t) {
            success = false;
        }
    }
} catch (e) {
   success = false;
}

if(success){
    vars.put("retry", "stop");
}