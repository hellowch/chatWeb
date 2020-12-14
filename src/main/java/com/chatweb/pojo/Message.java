package com.chatweb.pojo;

import com.google.gson.Gson;
import lombok.Data;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;


public class Message {

    //返回的话
    private String content;

    //ID和用户名
    private Map<String,String> names;

    //返回当前时间
    private Date date=new Date();

    private static Gson gson=new Gson();

    public String toJson(){
        return gson.toJson(this);
    }


    public Message(String content, Map<String, String> names) {
        this.content = content;
        this.names = names;
    }

    public Message() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public void setContent(String name,String msg) {
        this.content = name+" "+ DateFormat.getDateTimeInstance().format(date) +":<br/> "+msg;
    }

    public Map<String, String> getNames() {
        return names;
    }
    public void setNames(Map<String, String> names) {
        this.names = names;
    }


}
