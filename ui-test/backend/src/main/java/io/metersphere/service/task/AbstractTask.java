package io.metersphere.service.task;

import io.metersphere.base.domain.UiTaskRefresh;
import io.metersphere.base.mapper.UiTaskRefreshMapper;

import jakarta.annotation.Resource;
import java.util.UUID;

public abstract class AbstractTask {

    public static final Integer COMPLETE = 1;

    public static final Integer REFRESH = 2;

    @Resource
    private UiTaskRefreshMapper uiTaskRefreshMapper;

    public abstract String getKey();

    public UiTaskRefresh getTaskByKey(){
        return uiTaskRefreshMapper.getByTaskKey(getKey());
    }

    public abstract void doTask();

    public void saveRefreshStatus(UiTaskRefresh refresh, Integer status) {
        if(refresh == null){
            refresh = new UiTaskRefresh();
            refresh.setId(UUID.randomUUID().toString());
            refresh.setTaskKey(getKey());
            refresh.setCreateTime(System.currentTimeMillis());
            refresh.setTaskStatus(status);
            uiTaskRefreshMapper.insert(refresh);
            return;
        }

        //更新
        refresh.setTaskStatus(status);
        uiTaskRefreshMapper.update(refresh);
    }
}
