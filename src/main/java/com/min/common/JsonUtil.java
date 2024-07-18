package com.min.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;

import java.util.Map;

/**
 * @author wangmin
 * @date 2024/7/13 23:37
 */
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String parseJsonString(Object object){
        if(object == null)
            return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String str,Class<T> clazz){
        if(StringUtil.isNullOrEmpty(str))
            return null;
        try {
            return objectMapper.readValue(str,clazz);
        } catch (JsonProcessingException e) {
            LogUtil.log("json转对象失败: ",str);
            throw new RuntimeException(e);
        }
    }

    public static Object convertToType(Class clazz, Object object) {
        if (object instanceof Map) {
            JavaType type = objectMapper.getTypeFactory().constructType(clazz);
            return objectMapper.convertValue(object, type);
        }else {
            return object;
        }
    }
}
