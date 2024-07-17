package com.min.rpc.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.rpc.RpcSerializer;

import java.io.IOException;

/**
 * @author wangmin
 * @date 2024/7/13 22:31
 */
public class JsonRpcSerializer implements RpcSerializer {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object object) throws IOException {
        return objectMapper.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes,clazz);
    }
}
