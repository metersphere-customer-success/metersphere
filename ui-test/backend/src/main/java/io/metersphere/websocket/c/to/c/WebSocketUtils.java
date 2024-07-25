package io.metersphere.websocket.c.to.c;

import io.metersphere.utils.LoggerUtil;
import io.metersphere.websocket.c.to.c.util.MsgDto;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketUtils {
    public static final Map<String, Session> ONLINE_USER_SESSIONS = new ConcurrentHashMap<>();

    // 单用户推送
    public static void sendMessage(Session session, String message) {
        if (session == null) {
            return;
        }
        // 替换了web容器后 jetty没有设置永久有效的参数，这里暂时设置超时时间为一天
        session.setMaxIdleTimeout(86400000l);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async == null) {
            return;
        }
        async.sendText(message);
    }

    /**
     * 同步发送消息
     */
    public static void sendMessageForBasic(Session session, String message) {
        if (session == null) {
            return;
        }
        // 替换了web容器后 jetty没有设置永久有效的参数，这里暂时设置超时时间为一天
        session.setMaxIdleTimeout(86400000l);
        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        if (basicRemote == null) {
            return;
        }
        try {
            basicRemote.sendText(message);
        } catch (IOException ex) {
            LoggerUtil.error(String.format("ws basic msg send err ：message is %s", message), ex);
        }
    }

    /**
     * 单用户推送
     */
    public static void sendMessageSingleForBasic(MsgDto dto) {
        sendMessageForBasic(ONLINE_USER_SESSIONS.get(Optional.ofNullable(dto.getReportId()).orElse(StringUtils.EMPTY)), dto.getContent());
        sendMessageForBasic(ONLINE_USER_SESSIONS.get(Optional.ofNullable(dto.getToReport()).orElse(StringUtils.EMPTY)), dto.getContent());
    }

    // 单用户推送
    public static void sendMessageSingle(MsgDto dto) {
        sendMessage(ONLINE_USER_SESSIONS.get(Optional.ofNullable(dto.getReportId()).orElse(StringUtils.EMPTY)), dto.getContent());
        sendMessage(ONLINE_USER_SESSIONS.get(Optional.ofNullable(dto.getToReport()).orElse(StringUtils.EMPTY)), dto.getContent());
    }

    public static boolean has(String key) {
        return StringUtils.isNotEmpty(key) && ONLINE_USER_SESSIONS.containsKey(key);
    }

    // 全用户推送
    public static void sendMessageAll(String message) {
        ONLINE_USER_SESSIONS.forEach((sessionId, session) -> {
            sendMessage(session, message);
        });
    }

    //当前的Session 移除
    public static void onClose(String reportId) {
        try {
            if (WebSocketUtils.ONLINE_USER_SESSIONS.containsKey(reportId)) {
                WebSocketUtils.ONLINE_USER_SESSIONS.get(reportId).close();
                WebSocketUtils.ONLINE_USER_SESSIONS.remove(reportId);
            }
            if (WebSocketUtils.ONLINE_USER_SESSIONS.containsKey(("send." + reportId))) {
                WebSocketUtils.ONLINE_USER_SESSIONS.get(("send." + reportId)).close();
                WebSocketUtils.ONLINE_USER_SESSIONS.remove(("send." + reportId));
            }
        } catch (Exception e) {
            LoggerUtil.error("关闭socket失败：", e);
        }
    }
}
