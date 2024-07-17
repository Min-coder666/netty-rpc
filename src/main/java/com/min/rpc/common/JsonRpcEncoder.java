package com.min.rpc.common;

import com.min.common.JsonUtil;
import com.min.rpc.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author wangmin
 * @date 2024/7/17 23:14
 */
public class JsonRpcEncoder extends MessageToMessageEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest request, List<Object> list) throws Exception {
        String json = JsonUtil.parseJsonString(request);
        list.add(json);
    }
}
