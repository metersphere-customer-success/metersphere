var scriptString = (escapeScriptPreprocessor("${target}")).script.replace(/`/g, '\\`');
var argv = (escapeScriptPreprocessor("${target}")).argv;
driver.executeScript("" + scriptString + "", argv);
alertHandle();