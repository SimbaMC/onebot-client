package cn.evole.onebot.client.util;

import cn.evole.onebot.sdk.util.json.JsonsObject;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;

import java.io.IOException;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:06
 * Version: 1.0
 */
public class ActionSendUtils{
    private static final Logger log = LogManager.getLogger(ActionSendUtils.class);

    private final WebSocket channel;

    private final long requestTimeout;

    private JsonsObject resp;

    /**
     * @param channel        {@link WebSocket}
     * @param requestTimeout Request Timeout
     */
    public ActionSendUtils(WebSocket channel, Long requestTimeout) {
        this.channel = channel;
        this.requestTimeout = requestTimeout;
    }

    /**
     * @param req Request json data
     * @return Response json data
     * @throws IOException          exception
     * @throws InterruptedException exception
     */
    public JsonsObject send(JsonObject req) throws IOException, InterruptedException {
        synchronized (channel) {
            log.debug(String.format("[Action] %s", req.toString()));
            channel.send(req.toString());
        }
        synchronized (this) {
            this.wait(requestTimeout);
        }
        return resp;
    }

    /**
     * @param resp Response json data
     */
    public void onCallback(JsonsObject resp) {
        this.resp = resp;
        synchronized (this) {
            this.notify();
        }
    }
}