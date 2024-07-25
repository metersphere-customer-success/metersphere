package io.metersphere.utils;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.metersphere.commons.utils.JSON;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class ScopeVariableHandler {

    public ScopeVariableHandler() {
    }

    public Map<String, InheritScopeVariable> indexMap = new ConcurrentHashMap<>();

    /**
     * 后面追加, 返回子节点
     */
    public InheritScopeVariable push(String id, String pid, List<InheritScopeVariable.ScopedVar> varArr, Boolean enable, Boolean smartVariableEnable, Boolean environmentEnable, List<InheritScopeVariable.ScopedVar> envVarArr) {
        if (CollectionUtils.isNotEmpty(varArr)) {
            varArr = varArr.stream().filter(item -> item.enable).collect(Collectors.toList());
            varArr = varArr.stream().filter(item -> item.value != null && StringUtils.isNotBlank(item.name) && !String.format("${%s}", item.name).equals(item.value)).collect(Collectors.toList());
        }
        InheritScopeVariable sub = new InheritScopeVariable(id, varArr, enable, smartVariableEnable, environmentEnable, envVarArr);
        if (pid != null && indexMap.get(pid) != null) {
            sub.setParent(indexMap.get(pid));
        }
        indexMap.put(id, sub);
        return sub;
    }

    /**
     * Since V2.5
     *
     *  通过id 查找继承关系中JSONArray 并通过 enableFilter控制是否过滤 enable 状态为false的
     *
     * <p> options:  </p>
     *      1、使用原场景环境执行
     *      2、使用原场景变量
     *      3、优先使用当前场景变量，没有则使用原场景变量
     *
     *      -- 2、3 不可以同时选择
     *
     * <p> combination: 1 - checked  </p>
     *   ----------  ---------- ---------- ---------- ---------- ----------
     *      1:  0      1:  1      1:  1      1:  1      1:  0      1:  0
     *      2:  0      2:  0      2:  1      2:  0      2:  1      2:  0
     *      3:  0      3:  0      3:  0      3:  1      3:  0      3:  1
     *   ----------  ---------- ---------- ---------- ---------- ----------
     *
     * <p> rules: 针对于上述 6 种组合结果 (从左至右依次顺序) </p>
     *      <p> - 情况一: 都不勾选情况，使用 "当前场景" 的场景变量及 "当前场景" 的环境变量 </p>
     *      <p> - 情况二: 使用 "当前场景" 的场景变量及 "原场景" 的环境变量 </p>
     *      <p> - 情况三: 使用 "原场景" 的场景变量及 "原场景" 的环境变量 </p>
     *      <p> - 情况四: 优先使用 "当前场景" 的场景变量 及 "原场景" 的环境变量 （当前场景场景变量  --> 原场景场景变量 --> 原场景环境变量） </p>
     *      <p> - 情况五: 使用 "原场景" 的场景变量及 "当前场景" 的环境变量 </p>
     *      <p> - 情况六: 优先使用 "当前场景" 的场景变量 及 "当前场景" 的环境变量 （当前场景场景变量--> 原场景场景变量 --> 当前场景环境变量） </p>
     *
     * @Param id 场景或指令 id
     * @Return 当前作用域下的变量列表
     */
    public JSONArray findInheritJsonArrayById(String id) {
        InheritScopeVariable node = indexMap.get(id);
        JSONArray result = new JSONArray();
        Map<String, InheritScopeVariable.ScopedVar> tempCache = Maps.newConcurrentMap();

        do {
            List from = result.toList().stream().map(r -> ((JSONObject) r).get("name")).collect(Collectors.toList());

            List resultList = Lists.newArrayList();
            if (node != null && node.getVarArr() != null) {
                //过滤result 有的 result 中的优先级最高
                List<InheritScopeVariable.ScopedVar> list = node.getVarArr().stream().filter(v -> {
                    InheritScopeVariable.ScopedVar obj = v;
                    return !from.contains(obj.getName());
                }).collect(Collectors.toList());
                if (node.getEnable()) {
                    JSONArray filters = new JSONArray(JSON.toJSONString(list));
                    resultList = result.toList();
                    resultList.addAll(filters.toList());
                } else if (node.getSmart()) {
                    //智能合并
                    list.forEach(v -> tempCache.put(v.name, v));
                }

                if (CollectionUtils.isNotEmpty(resultList)) {
                    result = new JSONArray(resultList);
                }
            }
        }
        while (node != null && (!node.getEnable() || node.getSmart()) && ((node = node.hasParent()) != null));

        // 融入缓存的值
        List<Object> waitMerge = Lists.newArrayList();
        //找出 result 不存在 cache 中存在
        Set<String> cacheKeys = tempCache.keySet();
        Set<String> resultKeys = result.toList().stream().map(v ->
        {
            Object name = ((HashMap) v).get("name");
            if (Objects.isNull(name)) {
                return StringUtils.EMPTY;
            }
            return String.valueOf(name);
        })
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        Set<String> keys = cacheKeys.stream().filter(v -> !resultKeys.contains(v)).collect(Collectors.toSet());

        // 从缓存中提取
        keys.forEach(key -> {
            if (StringUtils.isNotBlank(key) && tempCache.get(key) != null) {
                waitMerge.add(tempCache.get(key).convert());
            }
        });
        for (Object wait : waitMerge) {
            result.put(wait);
        }


        return result;
    }


    @Getter
    @Setter
    public static class InheritScopeVariable {
        private InheritScopeVariable parent;
        private String id;
        private List<ScopedVar> varArr;
        private Boolean enable;
        private Boolean smart = false;
        /**
         * 是否使用原场景环境运行 环境变量处理
         */
        private Boolean useOriginEnvVars = false;

        private List<ScopedVar> envVarArr;

        @Data
        public static class ScopedVar {
            private String name;
            private String type;
            private Object value;
            private Boolean enable;

            public JSONObject convert() {
                return new JSONObject(this);
            }
        }

        public InheritScopeVariable() {
        }

        public InheritScopeVariable(String id, List<ScopedVar> varArr, Boolean enable, Boolean smartVariableEnable, Boolean useOriginEnvVars, List<ScopedVar> envVarArr) {
            this.id = id;
            this.varArr = varArr;
            this.enable = enable == null ? true : enable;
            this.smart = smartVariableEnable == null ? false : smartVariableEnable;
            this.useOriginEnvVars = useOriginEnvVars == null ? false : useOriginEnvVars;
            this.envVarArr = envVarArr;
        }

        public InheritScopeVariable hasParent() {
            if (this.getParent() != null) {
                return this.getParent();
            }
            return null;
        }
    }
}
