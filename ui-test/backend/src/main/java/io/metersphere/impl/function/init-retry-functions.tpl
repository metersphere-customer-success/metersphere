//ui 失败重试
String rt = vars.get("retryTimes");
vars.put("retry", "notStop");
if ( rt == null || rt == "" ) {
	vars.put("retryTimes", "0");
	if (0 == ${maxRetryTimes}) {
        vars.put("retry", "stop");
    }
} else {
	Integer retryTimes = Integer.parseInt(rt);
	retryTimes ++;
	if (retryTimes == ${maxRetryTimes}) {
	    vars.put("retry", "stop");
	}
	vars.put("retryTimes", Integer.toString(retryTimes));
}
if (0 != ${maxRetryTimes}) {
  if (vars.get("retryTimes") != "0") {
      log.info("重试" + vars.get("retryTimes") + "_【${scenarioName}】");
  } else {
      log.info("首次_【${scenarioName}】");
  }
}
