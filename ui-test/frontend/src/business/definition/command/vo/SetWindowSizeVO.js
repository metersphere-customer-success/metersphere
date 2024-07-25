import BaseVO from "./BaseVO";

export default class SetWindowSizeVO extends BaseVO {
    constructor() {
        super("SetWindowSize");
        this.coordX = null;
        this.coordY = null;
    }

    async validate() {
        if (!this.optContent) {
            return Promise.resolve(this.fail(
                this.buildMessage("optContent", "optContent cannot be empty")
            ));
        }

        if (this.optContent === "setSize") {
            return Promise.resolve(this.verifyEmpty(
                {
                    propName: "coordX",
                    propValue: this.coordX,
                },
                {
                    propName: "coordY",
                    propValue: this.coordY,
                }
            ));
        }

        return Promise.resolve(this.success());
    }
}
