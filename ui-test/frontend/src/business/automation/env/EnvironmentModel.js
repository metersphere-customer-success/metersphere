import {get, post} from "metersphere-frontend/src/plugins/request"

/* ---------- Functions ------- */

export function compatibleWithEnvironment(environment) {
    //兼容旧版本
    if (!environment.config) {
        let config = new Config();
        if (!(environment.variables instanceof Array)) {
            config.commonConfig.variables = JSON.parse(environment.variables);
        }
        if (environment.hosts && !(environment.hosts instanceof Array)) {
            config.commonConfig.hosts = JSON.parse(environment.hosts);
            config.commonConfig.enableHost = true;
        }
        if (!(environment.headers instanceof Array)) {
            config.httpConfig.headers = JSON.parse(environment.headers);
        }
        config.httpConfig.port = environment.port;
        config.httpConfig.protocol = environment.protocol;
        config.httpConfig.domain = environment.domain;
        config.httpConfig.socket = environment.socket;
        config.httpConfig.description = environment.description;
        environment.config = JSON.stringify(config);
    }
}

export function parseEnvironment(environment) {
    compatibleWithEnvironment(environment);
    if (!(environment.config instanceof Config)) {
        environment.config = new Config(JSON.parse(environment.config));
    }
}

export function environmentGetALl() {
    return post('/environment/group/get/all', {});
}

export function getEnvironmentMapByGroupId(id) {
    return get('/environment/group/map/' + id);
}
