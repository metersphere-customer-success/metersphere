import BaseVO from "./BaseVO";

export default class SelectWindowVO extends BaseVO {
    constructor() {
        super("SelectWindow");
        this.handleId = null;
        this.handleName = null;
        this.handleIndex = null;
    }

    async validate() {
        if (!this.optContent) {
            return Promise.resolve(this.fail(
                this.buildMessage("optContent", "optContent cannot be empty")
            ));
        }

        if (this.optContent === "switchTheWindowById") {
            return Promise.resolve(this.verifyEmpty({
                propName: "handleId",
                propValue: this.handleId,
            }));
        } else if (this.optContent === "switchTheWindowByIndex") {
            return Promise.resolve(this.verifyEmpty({
                propName: "handleIndex",
                propValue: this.handleIndex,
            }));
        }

        return Promise.resolve(this.success());
    }
}
