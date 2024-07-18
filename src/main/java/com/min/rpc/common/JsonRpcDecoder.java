package com.min.rpc.common;

import com.min.common.JsonUtil;
import com.min.common.LogUtil;
import com.min.rpc.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.StringUtil;

import java.util.Objects;

/**
 * @author wangmin
 * @date 2024/7/13 22:31
 */
public class JsonRpcDecoder extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String jsonStr = (String) msg;
        RpcRequest request = JsonUtil.parseObject(jsonStr,RpcRequest.class);
        if(Objects.nonNull(request))
            ctx.fireChannelRead(request);
    }
}
