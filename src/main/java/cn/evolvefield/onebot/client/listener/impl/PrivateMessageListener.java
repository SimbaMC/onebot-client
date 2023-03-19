package cn.evolvefield.onebot.client.listener.impl;

import cn.evolvefield.onebot.client.handler.Handler;
import cn.evolvefield.onebot.client.listener.DefaultHandlerListener;
import cn.evolvefield.onebot.client.model.event.message.PrivateMessageEvent;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 16:29
 * Version: 1.0
 */
public class PrivateMessageListener  extends DefaultHandlerListener<PrivateMessageEvent> {
    @Override
    public void onMessage(PrivateMessageEvent privateMessage) {
        //处理逻辑
        String message = privateMessage.getMessage();
        String[] split = message.split(" ");
        String key = split[0];
        Handler<PrivateMessageEvent> handler = getHandler(key);
        if (handler != null) {
            handler.handle(privateMessage);
        }
    }
}