package com.wzq.rpc.rpcprovider.handler;

import com.alibaba.fastjson.JSON;
import com.wzq.rpc.common.RpcRequest;
import com.wzq.rpc.common.RpcResponse;
import com.wzq.rpc.rpcprovider.anno.RpcService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义Handler类，这个类需要做的事情有：
 *
 * <ul>
 *     <li>将标有@RpcService的注解的bean进行缓存</li>
 *     <li>接收客户端的请求</li>
 *     <li>根据传递过来的beanName从缓冲中查找</li>
 *     <li>通过反射调用bean的方法</li>
 *     <li>给客户端响应</li>
 * </ul>
 *
 * @author wzq
 * @create 2022-11-28 16:32
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<String> implements ApplicationContextAware {

    // 缓存的标注有RpcService注解的方法
    Map<String, Object> SERVICE_INSTANCE_MAP = new HashMap<>();

    /**
     * 将标有@RpcService的注解的bean进行缓存
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 通过注解获取bean集合
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(RpcService.class);

        // 循环遍历
        for (Map.Entry<String, Object> entry : serviceMap.entrySet()) {
            Object serviceBean = entry.getValue();

            // 被RpcService标注的Bean必须实现一个接口
            if (serviceBean.getClass().getInterfaces().length == 0) {
                throw new RuntimeException("对外暴露的服务必须实现接口");
            }

            // 默认处理 第一个被实现接口名称 作为缓存bean的名字
            String serviceName = serviceBean.getClass().getInterfaces()[0].getName();
            // 添加到Map中
            SERVICE_INSTANCE_MAP.put(serviceName, serviceBean);
        }

    }

    /**
     * 读取通道中的就绪事件——读取客户端信息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 接收客户端请求，被传递过来的时RpcRequest的JSON形式，因此需要事先转换
        RpcRequest rpcRequest = JSON.parseObject(msg, RpcRequest.class);

        // 先构建一个RpcResponse
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());

        // 业务处理
        try {
            // 调用handler方法，处理结果
            rpcResponse.setResult(handler(rpcRequest));
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出现异常，设置错误信息
            rpcResponse.setError(e.getMessage());
        }

        // 给客户端响应
        ctx.writeAndFlush(JSON.toJSONString(rpcResponse));
    }

    /**
     * <ul>
     *     <li>根据传递过来的beanName从缓冲中查找</li>
     *     <li>通过反射调用bean的方法</li>
     * </ul>
     *
     * @param rpcRequest
     * @return
     */
    private Object handler(RpcRequest rpcRequest) throws Exception {
        // 根据传递过来的beanName从缓冲中查找
        Object serviceBean = SERVICE_INSTANCE_MAP.get(rpcRequest.getClassName());

        // 没有查找到对应的服务
        if (serviceBean == null) {
            throw new RuntimeException("服务端没有找到服务");
        }

        // 通过反射调用bean的方法
        Class<?> clz = serviceBean.getClass();
        Method method = clz.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        return method.invoke(clz);
    }

}
