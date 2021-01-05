package com.medfav.bing.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket服务端
 * Create by wzx on 2020/12/23 10:54
 */
@Component
@ServerEndpoint(value = "/ws/asset")
@Slf4j
public class WebSocketServer {
    @PostConstruct
    public void init(){
        log.info("加载 WebSocket");
    }

    public static final AtomicInteger onlineCount = new AtomicInteger();
    //concurrent包的线程安全Set，用来存放每个客户端对应的Session对象。
    public static CopyOnWriteArraySet<Session> sessionSet = new CopyOnWriteArraySet<>();

    /**
     * 连接建立成功调用的方法
     * @param session
     */
    @OnOpen
    public void onOpen(Session session){
        sessionSet.add(session);
        int cnt = onlineCount.incrementAndGet();
        log.info("有新连接加入，当前连接数为：{}",cnt);
        SendMessage(session,"连接成功!");
    }

    /**
     * 连接关闭调用的方法
     * @param session
     */
    @OnClose
        public void onClose(Session session) {
        sessionSet.remove(session);
        int cnt = onlineCount.decrementAndGet();
        log.info("有连接关闭，当前连接数为：{}", cnt);
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端的消息：{}",message);
        SendMessage(session, "收到消息，消息内容："+message);

    }

    /**
     * 出现错误
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误：{}，Session ID： {}",error.getMessage(),session.getId());
        error.printStackTrace();
    }

    /**
     * 发送消息，实践表明，每次浏览器刷新，session会发生变化。
     * @param session
     * @param message
     */
    public static void SendMessage(Session session,String message) {
        try {
            session.getBasicRemote().sendText(String.format("{\"text\":\"%s\",\"from\":\"%s\"}",message,session.getId()));
        }catch (IOException e){
            log.error("发送消息出错：{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 群发消息
     * @param message
     * @throws IOException
     */
    public static void BroadCastInfo(String message) throws IOException {
        for (Session session : sessionSet) {
            if(session.isOpen()){
                SendMessage(session, message);
            }
        }
    }

    /**
     * 指定Session发送消息
     * @param sessionId
     * @param message
     * @throws IOException
     */
    public static void SendMessage(String message,String sessionId) throws IOException {
        Session session = null;
        for (Session s : sessionSet) {
            if(s.getId().equals(sessionId)){
                session = s;
                break;
            }
        }
        if(session!=null){
            SendMessage(session, message);
        }
        else{
            log.warn("没有找到你指定ID的会话：{}",sessionId);
        }
    }
}
