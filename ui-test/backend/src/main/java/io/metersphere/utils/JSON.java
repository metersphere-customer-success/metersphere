package io.metersphere.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.SystemConstants;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static io.metersphere.constants.TemplateConstants.FUNCTION_CONVERT_JACKSON;
import static io.metersphere.constants.TemplateConstants.FUNCTION_TYPE;

public class JSON implements BeanPostProcessor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();
    private static final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName(SystemConstants.JSENGINE_GRAAL);

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 自动检测所有类的全部属性
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public static String toJSONString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object parseObject(String content) {
        return parseObject(content, Object.class);
    }

    public static <T> T parseObject(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String content, TypeReference<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(InputStream src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List parseArray(String content) {
        return parseArray(content, Object.class);
    }

    public static <T> List<T> parseArray(String content, Class<T> valueType) {
        CollectionType javaType = typeFactory.constructCollectionType(List.class, valueType);
        try {
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String content, TypeReference<T> valueType) {
        try {
            JavaType subType = typeFactory.constructType(valueType);
            CollectionType javaType = typeFactory.constructCollectionType(List.class, subType);
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map parseMap(String jsonObject) {
        try {
            return objectMapper.readValue(jsonObject, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理jackson数据
     */
    public static synchronized String convertJacksonType(String originalString) {
        Map<String, String> uiCommonFuntionMap = (Map<String, String>) CommonBeanFactory.getBean("uiCommonFunctionMap");
        try {
            jsEngine.eval(uiCommonFuntionMap.get(FUNCTION_CONVERT_JACKSON));
            Invocable invocable = (Invocable) jsEngine;
            Object result = invocable.invokeFunction("adaptToJackson", originalString);
            if (result != null) {
                return String.valueOf(result);
            }
            return "";
        } catch (Exception e) {
            LoggerUtil.error("解析转换jackson函数出错", e);
        }
        return "";
    }

    /**
     * 获取场景变量输入字符串的实际类型
     *
     * @param originalString
     * @return
     */
    public static synchronized String getJSONType(String originalString) {
        Map<String, String> uiCommonFuntionMap = (Map<String, String>) CommonBeanFactory.getBean("uiCommonFunctionMap");
        try {
            jsEngine.eval(uiCommonFuntionMap.get(FUNCTION_TYPE));
            Invocable invocable = (Invocable) jsEngine;
            Object result = invocable.invokeFunction("getValidType", TemplateUtils.resetQuotes(originalString));
            if (result != null) {
                return String.valueOf(result);
            }
            return "CONSTANT";
        } catch (Exception e) {
            LoggerUtil.error("getJSONType函数出错", e);
        }
        return "CONSTANT";
    }


}
