package io.metersphere.db.service;

import com.baomidou.mybatisplus.extension.service.IService;

import io.metersphere.db.entity.CovsApp;

public interface CovsAppService extends IService<CovsApp> {
    public CovsApp getAppById(int id);
}
