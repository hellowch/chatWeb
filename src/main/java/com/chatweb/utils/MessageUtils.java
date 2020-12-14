package com.chatweb.utils;

import com.chatweb.pojo.ResultMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//用于封装消息的工具类
public class MessageUtils {

    /**
     * @param isSystemMessage  判断是系统消息还是推送给某个人
     * @param fromName  发送消息的人的name，null表示为系统发送
     * @param message   要发送的消息
     */
    public static String getMessage(boolean isSystemMessage,String fromName,Object message){
        try {
            ResultMessage result = new ResultMessage();
            result.setSystem(isSystemMessage);
            result.setMessage(message);
            if (fromName != null){
                result.setFromName(fromName);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(result);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return null;
    }
}
