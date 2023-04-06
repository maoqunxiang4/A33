package com.fuchuang.A33.service.Impl.Security;

import com.alibaba.fastjson.JSON;
import com.fuchuang.A33.utils.ResponseUtils;
import com.fuchuang.A33.utils.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Result result = Result.fail(HttpStatus.FORBIDDEN.value(), "用户未授权");
        String jsonString = JSON.toJSONString(result);
        ResponseUtils.renderString(response,jsonString) ;
    }
}
