/**
 * 处理环境中匹配url规则
 * 1.ui只使用无或者path规则
 * @param target
 * @returns {*}
 */
function getEnvironmentUrl(target) {
    if (!sc.conditions || !sc.conditions.length) {
        return target;
    }
    var finalTarget = null;

    var noneCondition = null;
    for (var i = 0; i < sc.conditions.length; i++) {
        if (sc.conditions[i].type && sc.conditions[i].type == "NONE") {
            noneCondition = sc.conditions[i];
            sc.conditions.splice(i, 1);
            break;
        }
    }

    for (var i = 0; i < sc.conditions.length; i++) {
        //只匹配一条规则进行替换
        if (finalTarget) {
            break;
        }
        var thisCondition = sc.conditions[i];
        if (!thisCondition.details || !thisCondition.details.length) {
            continue;
        }
        var detail = thisCondition.details[0];
        if (!detail.name) {
            continue;
        }
        switch (thisCondition.type) {
            case "PATH":
                if (detail.value == "contains") {
                    if (target && target.indexOf(detail.name) != -1) {
                        finalTarget = replaceDomainWith(thisCondition, target);
                    }
                }
                if (detail.value == "equals") {
                    if (target && getDomainLast(target) == (detail.name)) {
                        finalTarget = replaceDomainWith(thisCondition, target);
                    }
                }
                break;
            case "NONE":
                break;
            default:
                break;
        }
    }

    if (!finalTarget) {
        //如果没有一条除NONE以外的规则，则使用 NONE 规则替换
        if (noneCondition) {
            finalTarget = replaceDomainWith(noneCondition, target);
        }
    }

    return finalTarget ? finalTarget : target;
}

function replaceDomainWith(condition, target) {
    if (!condition.socket) {
        return null;
    }
    var newDomain = condition.socket.startsWith("http") ? condition.socket : condition.protocol + "://" + condition.socket;
    //替换成新域名加原来的后缀路径
    var path = getDomainLast(target);
    newDomain += path.startsWith("/") ? path : "/" + path;
    return newDomain;
}

/**
 * 获取url的路径 比如 http://www.baidu.com/user/session1 获取到的就是 /user/session1 去掉前面的域名
 * @param condition
 * @returns {null|string|[{validator: function(*, *=, *): *, trigger: string, required}]|string|*|string}
 */
function getDomainLast(url) {
    if (!url) {
        return "";
    }

    if (url.indexOf("http") == -1) {
        return url;
    }

    var urls = url.split("/");
    if (urls.length <= 3 && !urlPattern.test(url)) {
        return url;
    }
    var path = "";
    for (var i = 3; i < urls.length; i++) {
        path += "/" + urls[i];
    }
    return path;
}

// test data

// conditions = [{"type":"PATH","details":[{"name":"1","value":"contains","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"valid":true,"file":false}],"protocol":"http","socket":"rdmetersphere.fit2cloud.com","domain":"rdmetersphere.fit2cloud.com","port":0,"headers":[{"name":null,"value":null,"type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"valid":false,"file":false}],"moduleIds":null},{"type":"NONE","details":[{"name":"","value":"contains","type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"valid":false,"file":false}],"protocol":"http","socket":"127.0.0.1:3000","domain":"127.0.0.1","port":3000,"headers":[{"name":null,"value":null,"type":null,"files":null,"description":null,"contentType":null,"enable":true,"urlEncode":false,"required":true,"min":null,"max":null,"valid":false,"file":false}],"moduleIds":null}]