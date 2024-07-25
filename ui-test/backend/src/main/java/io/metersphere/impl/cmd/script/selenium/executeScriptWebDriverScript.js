var value = variableParsing("${value}");

var scriptString = (escapeScriptPreprocessor("${target}")).script.replace(/`/g, '\\`');
var argv = (escapeScriptPreprocessor("${target}")).argv;
var result = driver.executeScript("" + scriptString + "", argv);
if(scriptString.indexOf("return ") !== -1){
    //vars.putObject("${" + value + "}", result);
    refreshProgramRegistryStack(value, result);
    variablesSet(value, result);
    handleOutputParam(value, result + "");
}
alertHandle();