package com.min.rpc.common;

import com.min.rpc.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

/**
 * @author wangmin
 * @date 2024/7/13 22:24
 */
public class RpcEncoder extends MessageToByteEncoder {

    private final Class<?> clazz;
    private final RpcSerializer rpcSerializer;

    public RpcEncoder(Class<?> clazz, RpcSerializer rpcSerializer) {
        this.clazz = clazz;
        this.rpcSerializer = rpcSerializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (Objects.nonNull(clazz) && clazz.isInstance(o)){
            byte[] bytes = rpcSerializer.serialize(o);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
