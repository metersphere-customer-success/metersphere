/**
 * 深度比较原场景与变更后的新场景(不是严格的深度比较，省略和跳过很多字段，由于历史数据和json格式化工具null和[]认定为一致的)
 * @param x
 * @param y
 * @returns {boolean}
 */
import i18n from "@/i18n/lang/i18n";

export default function deepCompareScenario(x, y) {
    let i, l, leftChain, rightChain;
    let jumpProp = ["index", "active", "disabled", "variableEnable", "atomicCommands", "screenshotConfig", "projectId"];
    let recursionProp = ["hashTree", "preCommands", "postCommands"];
    let equalMap = new Map();
    equalMap.set("[]", "null");

    function compare2Objects(x, y) {
        let p;

        if (isNaN(x) && isNaN(y) && typeof x === 'number' && typeof y === 'number') {
            return true;
        }

        if (x === y) {
            return true;
        }

        if ((typeof x === 'function' && typeof y === 'function') ||
            (x instanceof Date && y instanceof Date) ||
            (x instanceof RegExp && y instanceof RegExp) ||
            (x instanceof String && y instanceof String) ||
            (x instanceof Number && y instanceof Number)) {
            return x.toString() === y.toString();
        }

        if (!(x instanceof Object && y instanceof Object)) {
            return false;
        }
        // eslint-disable-next-line
        if (x.isPrototypeOf(y) || y.isPrototypeOf(x)) {
            return false;
        }

        if (x.prototype !== y.prototype) {
            return false;
        }

        if (leftChain.indexOf(x) > -1 || rightChain.indexOf(y) > -1) {
            return false;
        }

        for (p in y) {
            if (jumpProp.indexOf(p) != -1) {
                continue;
            }
            if (recursionProp.indexOf(p) != -1) {
                if (!y[p] || !y[p].length) {
                    continue;
                }
            }
            // eslint-disable-next-line
            if (y.hasOwnProperty(p) !== x.hasOwnProperty(p)) {
                if (!y[p] || x[p]) {
                    return true;
                }
                return false;
            } else if (typeof y[p] !== typeof x[p]) {
                return false;
            }
        }

        for (p in x) {
            if (jumpProp.indexOf(p) != -1) {
                continue;
            }
            if (recursionProp.indexOf(p) != -1) {
                if (!x[p] || !x[p].length) {
                    continue;
                }
            }
            // eslint-disable-next-line
            if (y.hasOwnProperty(p) !== x.hasOwnProperty(p)) {
                if (!y[p] || !x[p]) {
                    return true;
                }
                return false;
            } else if (typeof y[p] !== typeof x[p]) {
                return false;
            }

            switch (typeof (x[p])) {
                case 'object':
                case 'function':

                    leftChain.push(x);
                    rightChain.push(y);

                    if (!compare2Objects(x[p], y[p])) {
                        return false;
                    }

                    leftChain.pop();
                    rightChain.pop();
                    break;

                default:
                    if (x[p] !== y[p]) {
                        if (recursionProp.indexOf(p) != -1) {
                            if (!x[p] || !x[p].length || !y[p] || !y[p].length) {
                                continue;
                            }
                        }
                        if (x.viewType && y.viewType) {
                            if (p == "name") {
                                if (i18n.t("ui." + x[p]) == y[p].trim() || i18n.t("ui." + y[p]) == x[p].trim()) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    if (arguments.length < 1) {
        return true;
    }

    for (i = 1, l = arguments.length; i < l; i++) {

        leftChain = [];
        rightChain = [];

        if (!compare2Objects(arguments[0], arguments[i])) {
            return false;
        }
    }

    return true;
}
