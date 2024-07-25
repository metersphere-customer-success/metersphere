import BaseVO from "./BaseVO";

export default class OpenVO extends BaseVO {
    constructor() {
        super("Open");
        this.webUrl = null;
    }

    async validate() {
        if (!this.webUrl) {
            return Promise.resolve(super.buildResult(
                false,
                super.buildMessage("webUrl", "url cannot be empty")
                )
            );
        } else {
            return Promise.resolve(super.success());
        }
    }
}
