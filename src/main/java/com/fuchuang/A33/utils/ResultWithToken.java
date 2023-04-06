package com.fuchuang.A33.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResultWithToken {
    private Integer state;
    private String message;
    private Object data;
    private String token = "null";

    public static ResultWithToken success(int state, Object data , String token){
        ResultWithToken result = new ResultWithToken();
        result.setData(data);
        result.setState(state);
        result.setToken(token);
        return result ;
    }

    public static ResultWithToken success(int state, Object data){
        ResultWithToken result = new ResultWithToken();
        result.setData(data);
        result.setState(state);
        return result ;
    }

    public static ResultWithToken success(int state){
        ResultWithToken result = new ResultWithToken();
        result.setState(state);
        return result ;
    }

    public static ResultWithToken fail(int state, String message){
        ResultWithToken result = new ResultWithToken();
        result.setMessage(message);
        result.setState(state);
        return result ;
    }

}
