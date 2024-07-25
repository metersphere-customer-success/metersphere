import {uuid} from "metersphere-frontend/src/model/ApiTestModel";

export default class BaseVO {
    constructor(type) {
        this.type = type;
        this.optContent = null;
        this.elementType = null;
        this.moduleId = "";
        this.elementId = null;
        this.locateType = null;
        this.viewLocator = null;
    }

    /**
     * 执行校验
     * @param {*} _id_ 当前步骤的标识 结果中tag返回
     * @param {*} _extend_ 扩展属性 传入后结果原样返回
     * @returns
     */
    async doValidate(_id_, _extend_) {
        this._id_ = _id_;
        this._extend_ = _extend_ || {};
        return await this.validate();
    }

    validate() {
        return Promise.resolve(this.success());
    }

    success() {
        return this.buildResult(true);
    }

    fail(...obj) {
        return this.buildResult(false, ...obj);
    }

    normalRule(arg) {
        return (
            !arg ||
            arg.propValue === "" ||
            arg.propValue === undefined ||
            arg.propValue === null
        );
    }

    /**
     * 校验参数是否为空
     * @param  {...any} args
     */
    verifyEmpty(...args) {
        let arr = args
            .filter((v) => (v.propRule ? v.propRule(v) : this.normalRule(v)))
            .map((v) => {
                return this.buildResult(
                    false,
                    this.buildMessage(
                        v.propName,
                        v.errMessage ? v.errMessage : v.propName + " cannot be empty"
                    )
                );
            });

        return this.composeResult(...arr);
    }

    buildResult(verify, ...results) {
        if (!results) {
            return {
                verify,
                result: [],
            };
        }
        return {
            verify,
            result: [...results],
        };
    }

    buildMessage(target, errMessage) {
        return {
            type: this.type,
            target: target,
            tag: this._id_,
            message: errMessage,
        };
    }

    elementValidate(source) {
        let target = source ? source : this;
        let elementType = "elementObject";
        if (target.elementType) {
            elementType = target.elementType;
        }
        if (elementType === "elementObject") {
            //校验元素对象
            return this.verifyEmpty(
                {
                    propName: "elementId",
                    propValue: target.elementId,
                },
                {
                    propName: "moduleId",
                    propValue: target.moduleId,
                }
            );
        }

        if (elementType === "elementLocator") {
            //校验元素定位
            return this.verifyEmpty(
                {
                    propName: "locateType",
                    propValue: target.locateType,
                },
                {
                    propName: "viewLocator",
                    propValue: target.viewLocator,
                }
            );
        }

        return this.success();
    }

    composeResult(...results) {
        //状态取 &
        //arr push
        let compose = this.success();
        for (let r of results) {
            compose.verify = compose.verify && r.verify;
            if (r.result && r.result.length > 0) {
                compose.result.push(...r.result);
            }
        }
        return compose;
    }

    /**
     * baseVO 作为 locator 基类，可以初始化用于存储坐标的数组
     */
    initCoord() {
        this.coords = [{index: uuid(), x: 0, y: 0, type: "Coord"}];
    }

    addCoord() {
        if (!this.coords) {
            this.coords = [{index: uuid(), x: 0, y: 0, type: "Coord"}];
        } else {
            this.coords.push([{index: uuid(), x: 0, y: 0, type: "Coord"}]);
        }
    }
}
