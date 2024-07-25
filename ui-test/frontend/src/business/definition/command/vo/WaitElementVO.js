import BaseVO from "./BaseVO";

export default class WaitElementVO extends BaseVO {
    constructor() {
        super("WaitElement");
        //等待文本
        this.optContent = "waitForText";
        this.waitText = null;
        this.waitTime = 15000;
    }

    async validate() {
        if (this.optContent === "waitForText") {
            return Promise.resolve(super.composeResult(
                super.elementValidate(),
                this.verifyEmpty({
                    propName: "waitText",
                    propValue: this.waitText,
                })
            ));
        }
        return Promise.resolve(super.composeResult(
            super.elementValidate(),
            this.verifyEmpty({
                propName: "waitTime",
                propValue: this.waitTime,
            })
        ));
    }
}
