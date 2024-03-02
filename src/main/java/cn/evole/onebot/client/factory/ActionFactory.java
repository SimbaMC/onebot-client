package cn.evole.onebot.client.factory;

import cn.evole.onebot.client.core.BotConfig;
import cn.evole.onebot.client.util.ActionUtils;
import cn.evole.onebot.sdk.action.ActionPath;
import cn.evole.onebot.sdk.util.json.GsonUtils;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;


/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:05
 * Version: 1.0
 */

public class ActionFactory {
    private static final Logger log = LogManager.getLogger("Action");
    /**
     * 请求回调数据
     */
    private final Map<String, ActionUtils> apiCallbackMap = new HashMap<>();
    /**
     * 用于标识请求，可以是任何类型的数据，OneBot 将会在调用结果中原样返回
     */
    private int echo = 0;

    @Getter
    private BotConfig config;

    public ActionFactory(BotConfig config){
        this.config = config;
    }

    /**
     * 处理响应结果
     *
     * @param respJson 回调结果
     */
    public void onReceiveActionResp(JsonObject respJson) {
        String echo = GsonUtils.getAsString(respJson, "echo");
        ActionUtils actionUtils = apiCallbackMap.get(echo);
        if (actionUtils != null) {
            // 唤醒挂起的线程
            actionUtils.onCallback(respJson);
            apiCallbackMap.remove(echo);
        }
    }

    /**
     * @param channel Session
     * @param action  请求路径
     * @param params  请求参数
     * @return 请求结果
     */
    public JsonObject action(WebSocket channel, ActionPath action, JsonObject params) {
        if (!channel.isOpen()) {
            return null;
        }
        val reqJson = generateReqJson(action, params);
        ActionUtils actionUtils = new ActionUtils(channel, 3000L);
        apiCallbackMap.put(reqJson.get("echo").getAsString(), actionUtils);
        JsonObject result = new JsonObject();
        try {
            result = actionUtils.send(reqJson);
        } catch (Exception e) {
            log.warn("Request failed: {}", e.getMessage());
            result.addProperty("status", "failed");
            result.addProperty("retcode", -1);
        }
        return result;
    }

    /**
     * 构建请求数据
     * {"action":"send_private_msg","params":{"user_id":10001000,"message":"你好"},"echo":"123"}
     *
     * @param action 请求路径
     * @param params 请求参数
     * @return 请求数据结构
     */
    private JsonObject generateReqJson(ActionPath action, JsonObject params) {
        val json = new JsonObject();
        json.addProperty("action", action.getPath());
        json.add("params", params);
        json.addProperty("echo", echo++);
        return json;
    }
}