package com.chatweb.pojo;

import lombok.Data;

//服务端发送给客户端的消息
@Data
public class ResultMessage {

    //如果为true表示群发，false表示单独发给某个人
    private boolean isSystem;

    //发信人用户名
    private String fromName;

    //消息
    private Object message;
}
