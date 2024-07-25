import {groupCommandDefinition} from "@/business/definition/command/all-command-definition";
import BaseVO from "@/business/definition/command/vo/BaseVO";
import commandVo from "@/business/definition/command/vo/command-vo";
import { cloneDeep } from "lodash-es"

/**
 *  scenario definition validator
 */
export default class definitionValidator {
  constructor(definition, vm, hashtree) {
    this.definition = definition;
    this.hashtree = hashtree;
    this.vm = vm;
    this.firstErr = undefined;
    this.errMap = new Map();
    this.errResourceIds = [];
    this.errPreOrPostResourceIds = [];
  }


  //校验
  async validate(byHashTree) {
    //无需校验
    if (!this.definition && !this.hashtree) {
      return this.definition;
    }

    //执行校验逻辑
    await this.verify(!byHashTree ? this.definition.hashTree : this.hashtree, "hashTree");

    //处理校验结果
    //将出错的步骤  递归向上找 折叠的要展开
    this.handleResult(!byHashTree ? this.definition.hashTree : this.hashtree,);

    //触发选中第一个报错的步骤
    //当前报错步骤中要是有前后置 中的错误 则 发出事件 默认选中前后置tab
    //检验结果
    return Promise.resolve(this.firstErr);
  }

    /**
     * 处理 结果数据
     */
    handleResult(hashTree, extractType, parent) {
        if (!hashTree) {
            return;
        }

        hashTree.forEach((v) => {
            if (!v.validateResult) {
                v.validateResult = {
                    verify: true,
                    hashTreeErr: false,
                    preCommandsErr: false,
                    postCommandsErr: false,
                    opt: "sys",
                };
            }
            //各司其职
            if (!v.validateResult.verify) {
                //记录第一个出现错误的位置
                if (!this.firstErr) {
                    this.firstErr = extractType ? parent : v;
                }
                this.errMap.set(v.id, v);
            }
            //处理前置
            let preErr = this.doHandleResult(
                v.preCommands,
                true,
                parent ? parent : v
            );

            //处理后置
            let postErr = this.doHandleResult(
                v.postCommands,
                true,
                parent ? parent : v
            );

            //处理 HashTree
            let hashTreeErr = this.doHandleResult(
                v.hashTree,
                extractType,
                parent ? parent : v
            );

            v.validateResult.verify =
                v.validateResult.verify &&
                !(preErr > 0 || postErr > 0 || hashTreeErr > 0);
            v.validateResult.hashTreeErr = hashTreeErr > 0;
            v.validateResult.preCommandsErr = preErr > 0;
            v.validateResult.postCommandsErr = postErr > 0;

            if (v.validateResult && !v.validateResult.verify) {
                //展开错误步骤
                v.expanded = true;
                v.active = true;
                extractType
                    ? this.errPreOrPostResourceIds.push(v.id)
                    : this.errResourceIds.push(v.id);
            }
            //校验前置脚本
            if (v.preCommands) {
                this.handleResult(v.preCommands, true, v);
            }

            //校验后置脚本
            if (v.postCommands) {
                this.handleResult(v.postCommands, true, v);
            }

            //递归hashTree
            if (v.hashTree) {
                this.handleResult(v.hashTree, extractType, v);
            }
        });
    }

    doHandleResult(hashTree, extractType, parent) {
        let times = 0;

        if (!hashTree) {
            return times;
        }

        hashTree.forEach((v) => {
            parent = extractType ? parent : v;
            if (v.validateResult && !v.validateResult.verify) {
                times++;

                //记录第一个出现错误的位置
                if (!this.firstErr) {
                    this.firstErr = extractType ? parent : v;
                }
                this.errMap.set(v.id, v);
            }

            if (v.hashTree) {
                times += this.doHandleResult(v.hashTree, extractType, parent);
            }

            if (v.preCommands) {
                times += this.doHandleResult(v.preCommands, true, parent);
            }

            if (v.postCommands) {
                times += this.doHandleResult(v.postCommands, true, parent);
            }
        });

        return times;
    }

    /**
     * 校验 definition参数
     * @param {*} hashTree
     * @param {*} type
     * @returns
     */
    async verify(hashTree, type, enable = true) {
        if (!hashTree || hashTree.length <= 0) {
            return Promise.resolve();
        }
        for (let i = 0; i < hashTree.length; i++) {
            await this._verify(hashTree[i], i, enable);
        }
        return Promise.resolve();
    }

    async _verify(v, i, enable) {
        //处理被禁用的步骤 没有名字的情况
        if (v.type === "MsUiCommand" && !v.enable && !v.name) {
            v.name = this.vm.$t(v.command);
        }

        //校验结果
        //状态为开启则继续校验
        let currentEnable = !enable ? enable : v.enable;
        await this.doVerify(v, currentEnable);

        //校验前置脚本
        if (v.preCommands) {
            await this.verify(v.preCommands, "preCommands", currentEnable);
        }

        //校验后置脚本
        if (v.postCommands) {
            await this.verify(v.postCommands, "postCommands", currentEnable);
        }

        //递归hashTree
        if (v.hashTree) {
            await this.verify(v.hashTree, "hashTree", currentEnable);
        }
        return Promise.resolve();
    }

    async doVerify(v, enable) {
        let validateResult = {
            verify: true,
            hashTreeErr: false,
            preCommandsErr: false,
            postCommandsErr: false,
        };

        let currentResult;

        if (v.type === "UiScenario" || v.type === "scenario" || v.type === "customCommand") {
            this.verify(v.hashTree, "hashTree");
            v.validateResult = currentResult || validateResult;
            return Promise.resolve();
        }

        let viewType = v.viewType;
        if (v.viewType === "dataExtraction" || v.viewType === "cmdExtraction") {
            viewType = "extraction";
        }
        let allDefinition = groupCommandDefinition[viewType];

        let result = allDefinition.find((item, index, arr) => {
            return item.command === v.command;
        });

        result = cloneDeep(result);
        //校验指令
        if (
            result &&
            result.vo &&
            Object.getPrototypeOf(result.vo).__proto__ === BaseVO.prototype
        ) {
            result = cloneDeep(result);
            Object.assign(result.vo, v.vo);
            currentResult = await result.vo.doValidate(v.id);
        } else if (v.vo) {
            let voDefinition = commandVo[v.command];
            if (!voDefinition) {
                voDefinition = commandVo[v.command + "VO"];
            }
            voDefinition = cloneDeep(voDefinition);
            if (!voDefinition) {
                console.log("没有获取到" + v.command + "的定义信息!");
                return Promise.resolve();
            }
            Object.setPrototypeOf(voDefinition, BaseVO.prototype);
            Object.assign(voDefinition, v.vo);
            currentResult = await voDefinition.doValidate(v.id);
        } else if (v.targetVO) {
            //pause 和 submit 单独校验 或者是自定义指令的校验逻辑
            //没有匹配到校验器, 即将匹配自定义校验规则
            currentResult = this.customValidate(v);
        }

        validateResult = currentResult || validateResult;
        if (v.type === "MsUiCommand") {
            //校验公共属性 ex: name
            let baseValidator = this.createBaseValidator();
            validateResult = baseValidator.composeResult(
                baseValidator.verifyEmpty({
                    propName: "name",
                    propValue: v.name,
                }),
                validateResult
            );
        }
        if (!enable) {
            v.validateResult = {
                verify: true,
                hashTreeErr: false,
                preCommandsErr: false,
                postCommandsErr: false,
            }
            return Promise.resolve();
        }
        v.validateResult = validateResult;
        return Promise.resolve();
    }

    //自定义校验器
    customValidate(data) {
        let base = this.createBaseValidator();
        if (data.command === "submit") {
            let target = data.targetVO;
            //基于BaseVo中的校验方法 进行自定义校验
            return base.elementValidate(target);
        }
    }

    createBaseValidator() {
        return new BaseVO();
    }
}
