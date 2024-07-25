import BaseVO from "./BaseVO";
import {fileExists} from "@/api/file";
import { reduce, filter, find } from "lodash-es";

export default class InputVO extends BaseVO {
    constructor() {
        super("Input");
        this.inputContent = null;
        this.addInput = false;
        this.files = null;
    }

    async validate() {
        let r = await this.validateFiles();
        return Promise.resolve(super.composeResult(
            super.elementValidate(),
            //有文件上传的时候不校验输入
            this.files && this.files.length ? r :
                this.verifyEmpty({
                    propName: "inputContent",
                    propValue: this.inputContent,
                })
        ));
    }

    async validateFiles() {
        if (!this.files || !this.files.length) {
            return Promise.resolve(this.success());
        }
        let ids = reduce(this.files, function (r, v) {
            if (r.indexOf(v.id) === -1) {
                r.push(v.id);
            }
            return r;
        }, []);
        let existFiles = await fileExists(ids);
        if (existFiles && existFiles.data) {
            ids = filter(ids, (id) => existFiles.data.indexOf(id) == -1);
            if (ids && ids.length) {
                let deletedFiles = reduce(this.files, (r, v) => {
                    if (ids.indexOf(v.id) != -1 && !find(r, {id: v.id})) {
                        r.push(v);
                    }
                    return r;
                }, []);
                let deletedFileNames = reduce(deletedFiles, (r, v) => {
                    r.push(v.name);
                    return r;
                }, []);
                return Promise.resolve(this.fail("文件: 【" + deletedFileNames + "】 已被删除！请重新选择文件！"));
            }
        }
        return Promise.resolve(this.success());
    }
}
