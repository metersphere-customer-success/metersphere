import BaseVO from "./BaseVO";

export default class SelectFrameVO extends BaseVO {
    constructor() {
        super("SelectFrame");
        this.frameIndex = null;
    }


    async validate() {
        if (!this.optContent) {
            return Promise.resolve(this.fail(
                this.buildMessage("optContent", "optContent cannot be empty")
                )
            );
        }

        if (this.optContent === "switchTheFrameByIndex") {
            return Promise.resolve(this.verifyEmpty({
                propName: "frameIndex",
                propValue: this.frameIndex,
            }));
        } else if (this.optContent === "default") {
            //
            return Promise.resolve(this.elementValidate());
        }
        return Promise.resolve(this.success());
    }

}
