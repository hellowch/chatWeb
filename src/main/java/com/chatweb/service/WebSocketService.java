package com.chatweb.service;


import com.chatweb.pojo.Message;
import com.chatweb.utils.MessageUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
@ServerEndpoint("/webSocket")
public class WebSocketService {

    //用户列表，存入每一个客户端用于访问的WebSocketService对象
    private static Map<String,WebSocketService> onLineUsers = new ConcurrentHashMap<>();

    //通过该对象发送消息给指定用户
    private Session session;

    private String username;

    Logger log= Logger.getLogger(String.valueOf(WebSocketService.class));

    //登录时调用
    @OnOpen
    public void onOpen(Session session){
        //将局部session对象赋值给成员session
        this.session=session;
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
        onLineUsers.put(username,this);
        log.info("有新连接，总数为:"+onLineUsers.size()+"---sessionId:"+session.getId()+"----"+username);
        String content="欢迎"+username+"进入聊天室！";
        //放入欢迎语、ID用户名
        String message = MessageUtils.getMessage(true,null,getNames());
        send(message);
    }

    private Set<String> getNames(){
        //单列集合，返回所有的用户名
        return onLineUsers.keySet();
    }

    //收到用户发送过来的信息时调用
    @OnMessage
    public void onMessage(String message,Session session){
        try {
            //将接收到的message字符串转换成message对象
            ObjectMapper mapper = new ObjectMapper();
            Message mess = mapper.readValue(message, Message.class);
            //获取要将数据发送给的用户
            String toName = mess.getToName();
            //获取消息数据
            String data = mess.getMessage();
            //获取当前登录用户name
            String username = URLDecoder.decode(session.getQueryString().split("=")[1],"UTF-8");

            //若toName为空，则选择群发消息,否则为私聊toName
            if (toName.equals("")){
                //获取推送给指定用户的消息格式数据
                String con = MessageUtils.getMessage(true,username,data);
                send(con);
            }else {
                String con = MessageUtils.getMessage(false,username,data);
                onLineUsers.get(toName).session.getBasicRemote().sendText(con);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //关闭时调用
    @OnClose
    public void onClose(Session session) throws UnsupportedEncodingException {
        //移除记录
        //获取当前登录用户name
        String username = URLDecoder.decode(session.getQueryString().split("=")[1],"UTF-8");
        //从容器中删除指定username
        onLineUsers.remove(username);
        //向其他用户推送该用户退出后的在线用户列表
        String con = MessageUtils.getMessage(true,null,getNames());
        send(con);
    }


    //向所有用户推送消息
    public void send(String message){
        //遍历每一名已登录的用户，发送消息
        try {
            Set<String> names = onLineUsers.keySet();
            for (String name : names){
                WebSocketService webSocketService = onLineUsers.get(name);
                webSocketService.session.getBasicRemote().sendText(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
