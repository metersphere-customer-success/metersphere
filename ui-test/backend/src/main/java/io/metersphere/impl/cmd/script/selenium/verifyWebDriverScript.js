var target = variableParsing("${${target}}", false);
var value = variableParsing("${value}");
var vo = ${vo};

var assertValue;
if (typeof target == "object") {
    assertValue = JSON.stringify(target);
} else if (typeof target == "boolean") {
    assertValue = target.toString();
}else {
    assertValue = target ? target.toString() : target;
}

WDS.log.info("verifyWeb");
WDS.log.info(assertValue);


if (typeof value == "object") {
    value = JSON.stringify(value);
} else if (typeof value == "boolean") {
    value = value.toString();
} else {
    value = value ? value.toString() : value;
}

WDS.log.info(value);


// 校验值是否相等（含对象）
switch (vo.optType) {
    case "equal" :
        if (assertValue != value) {
            throw new Error("Actual value '" + assertValue + "' did not match '" + value + "'");
        }
        break;
    case "notEqual" :
        if (assertValue == value) {
            throw new Error("Actual value '" + assertValue + "' matches '" + value + "'");
        }
        break;
    case "contain" :
        if (assertValue.indexOf(value) == -1) {
            throw new Error("Actual value '" + assertValue + "' did not contain '" + value + "'");
        }
        break;
    case "notContain" :
        if (assertValue.indexOf(value) != -1) {
            throw new Error("Actual value '" + assertValue + "' contains '" + value + "'");
        }
        break;
    case "greater" :
        if (isNaN(assertValue)) {
            throw new Error("Actual value '" + assertValue + "' is Not a number !");
        }
        if (isNaN(value)) {
            throw new Error("value '" + value + "' is Not a number !");
        }
        if (parseFloat(assertValue) <= parseFloat(value)) {
            throw new Error("Actual value '" + assertValue + "' is not greater than '" + value + "'");
        }
        break;
    case "greaterEqual" :
        if (isNaN(assertValue)) {
            throw new Error("Actual value '" + assertValue + "' is Not a number !");
        }
        if (isNaN(value)) {
            throw new Error("value '" + value + "' is Not a number !");
        }
        if (parseFloat(assertValue) < parseFloat(value)) {
            throw new Error("Actual value '" + assertValue + "' is not greater than or equal to '" + value + "'");
        }
        break;
    case "lower" :
        if (isNaN(assertValue)) {
            throw new Error("Actual value '" + assertValue + "' is Not a number !");
        }
        if (isNaN(value)) {
            throw new Error("value '" + value + "' is Not a number !");
        }
        if (parseFloat(assertValue) >= parseFloat(value)) {
            throw new Error("Actual value '" + assertValue + "' is not lower than '" + value + "'");
        }
        break;
    case "lowerEqual" :
        if (isNaN(assertValue)) {
            throw new Error("Actual value '" + assertValue + "' is Not a number !");
        }
        if (isNaN(value)) {
            throw new Error("value '" + value + "' is Not a number !");
        }
        if (parseFloat(assertValue) > parseFloat(value)) {
            throw new Error("Actual value '" + assertValue + "' is not lower than or equal to '" + value + "'");
        }
        break;
    case "null" :
        if (assertValue && assertValue.length) {
            throw new Error("Actual value '" + assertValue + "' is not null");
        }
        break;
    case "notNull" :
        if (!assertValue) {
            throw new Error("Actual value '" + assertValue + "' is null");
        }
        break;
}

