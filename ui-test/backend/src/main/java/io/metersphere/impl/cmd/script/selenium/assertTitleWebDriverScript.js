var target = variableParsing("${target}");

var element = driver.getTitle();
if (element != target) {
    throw new Error("Actual title '" + element + "' did not match '" + target + "'")
}