package com.min.rpc.client;

import com.min.common.JsonUtil;
import com.min.common.LogUtil;
import com.min.rpc.RpcClient;
import com.min.rpc.RpcRequest;
import com.min.rpc.common.GzipDecoder;
import com.min.rpc.common.GzipEncoder;
import com.min.rpc.common.JsonRpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import sun.rmi.runtime.Log;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * @author wangmin
 * @date 2024/7/13 23:40
 */
public class NettyRpcClient implements RpcClient, Callable<String> {
    private final static ExecutorService executorService =
            Executors.newCachedThreadPool();
//            new ThreadPoolExecutor(12,20,60,TimeUnit.SECONDS,new SynchronousQueue<>());
    private String result;

    private boolean wait = true;

    private Channel channel;

    private RpcRequest request;

    private final Object clock = new Object();

    public void run(int serverPort){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .remoteAddress("127.0.0.1",serverPort)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new LengthFieldPrepender(8))
                                .addLast(new GzipEncoder())
                                .addLast(new JsonRpcEncoder())
                                .addLast(new LengthFieldBasedFrameDecoder(4104, 0, 8, 0, 8))
                                .addLast(new GzipDecoder())
                                .addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                synchronized (clock){
                                    result = (String) msg;
                                    clock.notify();
                                }
                            }
                        });
                    }
                });

        ChannelFuture future = bootstrap.connect();
        future.addListener((ChannelFuture cf)->{
            if(cf.isSuccess()){
                LogUtil.log("客户端连接成功,服务端口:",serverPort);
            }else {
                LogUtil.log("连接失败,服务端口:",serverPort);
            }
        });
        try {
            future.sync();
            channel = future.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T call(String methodName, Object[] args,Class<T> clazz,boolean isWait) throws InterruptedException, ExecutionException, TimeoutException {
        request = new RpcRequest();
        request.setMethodName(methodName);
        request.setParameters(args);
        this.wait = isWait;
        Class[] paraType = new Class[args.length];
        for (int i = 0; i < paraType.length; i++) {
            paraType[i] = args[i].getClass();
        }
        request.setParameterTypes(paraType);
        String ret = executorService.submit(this).get();
        return JsonUtil.parseObject(ret,clazz);
    }

    @Override
    public String call() throws Exception {
        channel.writeAndFlush(request);
        this.result = null;
        if(wait)
            synchronized (clock){
                clock.wait();
            }
        return result;
    }
}
