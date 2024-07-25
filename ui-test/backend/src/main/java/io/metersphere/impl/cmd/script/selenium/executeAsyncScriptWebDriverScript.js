var target = variableParsing(Base64.decode("${target}"));
var value = variableParsing("${value}");

var scriptString = (escapeScriptPreprocessor("${target}")).script.replace(/`/g, '\\`');
var argv = (escapeScriptPreprocessor("${target}")).argv;

if(target.indexOf("return") !== -1){
    var result = driver.executeAsyncScript("window.setTimeout(arguments[arguments.length - 1], 2000);"
        + scriptString, argv);
    refreshProgramRegistryStack(value, result);
    variablesSet(value, result);
    var _asyncResult  = driver.executeScript("" + scriptString + "", argv);
    handleOutputParam(value, _asyncResult + "");
}else{
    driver.executeAsyncScript("window.setTimeout(arguments[arguments.length - 1], 5000);"
        + scriptString, argv);
}
alertHandle();
