var target = variableParsing("${target}");

var title = driver.getTitle();
if (title != target) {
    throw new Error("Actual title '" + title + "' did not match '" + target + "'")
}
