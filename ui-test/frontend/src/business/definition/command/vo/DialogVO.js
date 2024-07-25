import BaseVO from "./BaseVO";

export default class DialogVO extends BaseVO {
    constructor() {
        super("DialogVO");
        this.type = "Dialog";
        //操作方式 是 否
        this.inputOrNot = false;
        //输入内容
        this.inputContent = null;
        //操作方式 确定 取消
        this.optType = true;
    }

    async validate() {
        if (this.optType && this.inputOrNot) {
            return Promise.resolve(this.verifyEmpty({
                propName: "inputContent",
                propValue: this.inputContent,
            }));
        }
        return Promise.resolve(this.success());
    }
}
