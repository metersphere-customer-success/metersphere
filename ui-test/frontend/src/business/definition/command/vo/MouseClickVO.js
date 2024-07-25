import BaseVO from "./BaseVO";

export default class MouseClickVO extends BaseVO {
    constructor() {
        super("MouseClick");
        this.clickType = 'click';
        this.enableClickLocation = null;
        this.clickLocationX = null;
        this.clickLocationY = null;
    }

    validate() {
        return Promise.resolve(super.elementValidate());
    }
}
