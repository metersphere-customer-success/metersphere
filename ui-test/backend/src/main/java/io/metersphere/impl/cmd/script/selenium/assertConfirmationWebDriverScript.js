var target = variableParsing("${target}");

var text = alertResultText;
if (text !== target) {
    throw new Error("Actual alert confirmation '" + text + "' did not match '" + target + "'");
}