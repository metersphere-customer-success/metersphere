function typeConvert(val, type) {
    if (/^\\s{1,}\$/.exec(val)) {
        //判断是否为空白符
        return val;
    } else {
        if (type === "NUMBER") {
            if (IsNumber(val)) {
                return Number(val);
            }
        } else {
            if (IsJsonString(val)) {
                return JSON.parse(val);
            } else if (IsStr(val)) {
                return val;
            }
        }
    }
    return val;
}

function IsNumber(val) {
    try {
        return !isNaN(Number(val));
    } catch (e) {
        return false;
    }
}

function IsJsonString(str) {
    try {
       JSON.parse(str);
       if (!Number.isNaN(Number(str))) {
           return false;
       }
    } catch (e) {
        return false;
    }
    return true;
}

function IsOnlyJsonString(str) {
    try {
        var _str = JSON.parse(str);
        if (IsStr(_str)) {
            return false;
        }
    } catch (e) {
        return false;
    }
    return true;
}

function IsStr(str) {
    try {
        if (typeof str === "string") {
            return true;
        }
    } catch (e) {
        return false;
    }
    return false;
}

function IsArray(arr) {
    try {
        return Array.isArray(arr);
    } catch (e) {
        return false;
    }
}

/**
 * 根据字符串获取对应的类型
 * @return
 */
function getValidType(str) {
    try {
        if (!str) {
            //判断是否为空白符
            return "CONSTANT";
        } else if (IsJsonString(str)) {
            var obj = JSON.parse(str);
            if (obj instanceof Array) {
                return "ARRAY";
            }
            if (IsNumber(str)) {
                return "NUMBER";
            }
            return "JSON";
        } else if (IsNumber(str)) {
            return "NUMBER";
        }
        return "CONSTANT";
    } catch (e) {
        return "CONSTANT";
        // return e.toString();
    }
}