package com.min.rpc.provider;

import com.min.common.JsonUtil;
import com.min.common.LogUtil;
import com.min.rpc.RpcProvider;
import com.min.rpc.RpcRequest;
import com.min.rpc.common.GzipDecoder;
import com.min.rpc.common.GzipEncoder;
import com.min.rpc.common.JsonRpcDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author wangmin
 * @date 2024/7/13 22:22
 */
public class NettyRpcProvider implements RpcProvider {

    private final int port;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public NettyRpcProvider(int port) {
        this.port = port;
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            new Thread(this::server).start();
        }
    }

    private void server(){
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(4104, 0, 8, 0, 8))
                                .addLast(new GzipDecoder())
                                .addLast(new JsonRpcDecoder())
                                .addLast(new LengthFieldPrepender(8))
                                .addLast(new GzipEncoder())
                                .addLast(new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ctx.writeAndFlush(invoke(msg));
                                    }
                                });
                    }
                });

        try {
            ChannelFuture future= bootstrap.bind(port).sync();
            LogUtil.log("Rpc provider start success. Port is "+future.channel().localAddress());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }


    @Override
    public Object invoke(Object o) {
        if(!(o instanceof RpcRequest)){
            throw new RuntimeException("Rpc 请求调用参数错误");
        }

        RpcRequest request = (RpcRequest) o;
        String methodName = request.getMethodName();
        try {
            Method targetMethod = this.getClass().getMethod(methodName,request.getParameterTypes());
            targetMethod.setAccessible(true);
            convertRequestParams(request);
            Object object = targetMethod.invoke(this,request.getParameters());
            return JsonUtil.parseJsonString(object);
        } catch (Exception e) {
            LogUtil.log("invoke method-"+methodName+" fail ");
            throw new RuntimeException(e);
        }
    }

    private void convertRequestParams(RpcRequest request){
        Class[] clazzs = request.getParameterTypes();
        Object[] params = request.getParameters();
        for(int i=0;i<clazzs.length;i++){
            params[i] = JsonUtil.convertToType(clazzs[i],params[i]);
        }
    }
}
