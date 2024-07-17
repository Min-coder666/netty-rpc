package com.min.rpc.common;

import com.min.rpc.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author wangmin
 * @date 2024/7/13 22:25
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private final Class<?> clazz;
    private final RpcSerializer rpcSerializer;

    public RpcDecoder(Class<?> clazz, RpcSerializer rpcSerializer) {
        this.clazz = clazz;
        this.rpcSerializer = rpcSerializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int dataLength = byteBuf.readInt();
        // 我们读到的消息体长度为 0，这是不应该出现的情况，这里出现这情况，关闭连接。
        if (dataLength < 0) {
            channelHandlerContext.close();
        }
        //取出数据
        byte[] bytes = new byte[dataLength];
        byteBuf.readBytes(bytes);  //
        //将 byte 数据转化为我们需要的对象。
        Object object = rpcSerializer.deserialize(clazz,bytes);
        list.add(object);
    }
}
