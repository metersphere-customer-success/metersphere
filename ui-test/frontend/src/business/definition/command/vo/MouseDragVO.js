import BaseVO from "./BaseVO";

export default class MouseDragVO extends BaseVO {
    constructor() {
        super("MouseDrag");
        this.startLocator = new BaseVO();
        this.endLocator = new BaseVO();
        //初始化坐标数组
        this.startLocator.initCoord();
        this.endLocator.initCoord();
        //拖拽方式 内部拖拽 true 允许内部拖拽
        this.innerDrag = false;
        //是否设置坐标 true 支持
        this.setCoord = false;

    }

    async validate() {
        //内部拖拽只校验开始定位器
        if (this.innerDrag) {
            return Promise.resolve(super.composeResult(super.elementValidate(this.startLocator)));
        }
        return Promise.resolve(super.composeResult(super.elementValidate(this.startLocator), super.elementValidate(this.endLocator)));
    }
}
