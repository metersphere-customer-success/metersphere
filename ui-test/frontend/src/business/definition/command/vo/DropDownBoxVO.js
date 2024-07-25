import BaseVO from "./BaseVO";

export default class DropDownBoxVO extends BaseVO {
    constructor() {
        super("DropdownBox");
        this.optContent = "select";
        this.subItemType = null;
        this.subItem = null;
    }

    async validate() {
        return Promise.resolve(super.composeResult(
            super.elementValidate(),
            this.verifyEmpty(
                {
                    propName: "subItemType",
                    propValue: this.subItemType,
                },
                {
                    propName: "subItem",
                    propValue: this.subItem,
                }
            )
        ));
    }
}
