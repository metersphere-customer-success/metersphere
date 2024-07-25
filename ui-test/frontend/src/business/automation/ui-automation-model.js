import {
    BaseConfig, KeyValue
} from "@/api/ApiTestModel";
import {uuid} from "metersphere-frontend/src/model/ApiTestModel";

export class ScenarioConfig extends BaseConfig {
    constructor(options = {}) {
        super();
        this.set(options);
        //使用免登录
        this.useCookie = false;
    }
}

export function setScenarioConfig(scenario, vue) {
    if (scenario && !scenario.scenarioConfig) {
        vue.$set(scenario, "scenarioConfig", new ScenarioConfig());
    }
}

export class UiScenario extends BaseConfig {
    constructor(options = {}) {
        super();
        this.id = undefined;
        this.name = undefined;
        this.variables = [];
        this.environmentId = undefined;
        this.environment = undefined;
        this.enable = true;
        //chrome
        this.browser = 0;
        //场景其他配置项
        this.scenarioConfig = new ScenarioConfig();

        this.set(options);
        this.sets({
            variables: KeyValue,
        }, options);
    }

    initOptions(options = {}) {
        options.id = options.id || uuid();
        return options;
    }

    clone() {
        let clone = new UiScenario(this);
        clone.id = uuid();
        return clone;
    }

    isValid() {
        // if (this.enable) {
        //   for (let i = 0; i < this.requests.length; i++) {
        //     let validator = this.requests[i].isValid(this.environmentId, this.environment);
        //     if (!validator.isValid) {
        //       return validator;
        //     }
        //   }
        // }
        return {isValid: true};
    }

    isReference() {
        return this.id.indexOf("#") !== -1
    }
}


export class MsUiCommand extends BaseConfig {

    constructor(options = {}) {
        super();
        this.queryTimeout = options.queryTimeout || 60000;

        // this.sets({args: KeyValue, attachmentArgs: KeyValue, variables: KeyValue}, options);
    }

    isValid() {
        if (this.enable) {
            console.log("d")

        }
        return {
            isValid: true
        }
    }
}

export default class UiAutomationModel {
}