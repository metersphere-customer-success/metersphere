package io.metersphere.db.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.metersphere.db.entity.CovsApp;
import io.metersphere.db.mapper.CovsAppMapper;
import io.metersphere.db.service.CovsAppService;

@Service
public class CovsAppServiceImpl extends ServiceImpl<CovsAppMapper, CovsApp> implements CovsAppService {
    private QueryWrapper<CovsApp> newWrapper() {
        return new QueryWrapper<CovsApp>();
    }

    public CovsApp getAppById(int id) {
        return this.getOne(newWrapper().eq("id", id));
    }
}
