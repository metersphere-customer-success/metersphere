package io.metersphere.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.db.entity.CovsApp;
import io.metersphere.db.service.CovsAppService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/testutils/check")
public class CheckController {
    @Autowired
    private CovsAppService covsAppService;

    @RequestMapping(value = { "/", "" }, method = { RequestMethod.POST, RequestMethod.GET })
    public String check() {
        if (covsAppService == null) {
            LogUtil.debug("====== covsapp service is null");
            return "covsapp service is null";
        }

        // CovsApp covsApp = covsAppService.getById(1);
        CovsApp covsApp = covsAppService.getAppById(1);
        if (covsApp == null) {
            LogUtil.debug("====== covsapp is null: 1");
            return "covsapp is null: 1";
        }

        LogUtil.debug(String.format("====== %s", covsApp.toString()));
        return covsApp.toString();
    }
}
