import BaseVO from "./BaseVO";

export default class SetItemVO extends BaseVO {
    constructor(type) {
        super(type || "SetItem");
        this.optContent = "check";
    }

    async validate() {
        return Promise.resolve(super.elementValidate());
    }
}
