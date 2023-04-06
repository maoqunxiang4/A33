package com.fuchuang.A33.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtils {
    public static String renderString(HttpServletResponse response , String json){
        try{
            response.setStatus(200);
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json");
            response.getWriter().print(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null ;
    }
}
