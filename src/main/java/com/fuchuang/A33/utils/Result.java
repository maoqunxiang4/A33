package com.fuchuang.A33.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Result {
    private Integer state;
    private String message;
    private Object data;

    public static Result success(int state, Object data , String token){
        Result result = new Result();
        result.setData(data);
        result.setState(state);
        return result ;
    }

    public static Result success(int state, Object data){
        Result result = new Result();
        result.setData(data);
        result.setState(state);
        return result ;
    }

    public static Result success(int state){
        Result result = new Result();
        result.setState(state);
        return result ;
    }

    public static Result fail(int state, String message){
        Result result = new Result();
        result.setMessage(message);
        result.setState(state);
        return result ;
    }

}
