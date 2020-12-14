package com.chatweb.service;


import com.chatweb.pojo.Message;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

@Component
@ServerEndpoint("/webSocket")
public class WebSocket {

    //可以理解为线程安全的HashSet,存入访问用户的session数据
    private static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<>();
    //用户列表，存入访问用户的id和username
    private static Map<String,String> onLineUsers = new ConcurrentHashMap<>();

    private Session session;
    private String username;

    Logger log= Logger.getLogger(String.valueOf(WebSocket.class));

    //登录时调用
    @OnOpen
    public void onOpen(Session session){
        //将局部session对象赋值给成员session
        this.session=session;
        webSockets.add(this);
        //获取用户名
        String s=session.getQueryString(); //获取查询字符串，例http://localhost/test?a=b获取到a=b
        String urlUsername=s.split("=")[1]; //例如返回数据为"name=abc",则以=为界拆分成数组，取1号位置abc
        try {
            username=URLDecoder.decode(urlUsername,"UTF-8");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //把SessionID和用户名放进集合里面
        onLineUsers.put(session.getId(),username);
        log.info("有新连接，总数为:"+webSockets.size()+"sessionId:"+session.getId()+" "+username);
        String content="欢迎"+username+"进入聊天室！";
        //放入欢迎语、ID用户名
        Message message=new Message(content,onLineUsers);
        send(message.toJson());
    }

    private static Gson gson=new Gson();

    //向所有用户推送消息
    public void send(String message){
        //遍历每一名已登录的用户，发送消息
        for (WebSocket webSocket:webSockets){
            try {
                //推送消息
                webSocket.session.getBasicRemote().sendText(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //收到用户发送过来的信息时调用
    @OnMessage
    public void onMessage(String json){

    }

    //关闭时调用
    @OnClose
    public void onClose(){
        //移除记录
        webSockets.remove(this);
        onLineUsers.remove(session.getId());
        log.info("有新的断开，总数"+webSockets.size()+"sessionId:"+session.getId());
        String content="恭送"+username+"离开聊天室！";
        //向其他用户广播该用户退出信息
        Message message=new Message(content,onLineUsers);
        send(message.toJson());
    }



}
