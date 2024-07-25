import BaseVO from "./BaseVO";

export default class MouseMoveVO extends BaseVO {
    constructor() {
        super("MouseMove");
        this.moveType = "mouseOut";
        this.moveLocationX = null;
        this.moveLocationY = null;
    }

    async validate() {
        if (this.moveType === "mouseMoveAt") {
            return Promise.resolve(super.composeResult(
                super.elementValidate(),
                super.verifyEmpty(
                    {
                        propName: "moveLocationX",
                        propValue: this.moveLocationX,
                    },
                    {
                        propName: "moveLocationY",
                        propValue: this.moveLocationY,
                    }
                )
            ));
        }
        return Promise.resolve(super.elementValidate());
    }
}
