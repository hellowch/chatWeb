package com.chatweb.pojo;

import lombok.Data;

@Data
public class Result {

    //false表示用户名或者密码错误
    private boolean flag;

    //返回给游览器提示
    private String message;
}
