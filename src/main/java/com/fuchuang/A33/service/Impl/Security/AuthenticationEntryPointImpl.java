package com.fuchuang.A33.service.Impl.Security;

import com.alibaba.fastjson.JSON;
import com.fuchuang.A33.utils.ResponseUtils;
import com.fuchuang.A33.utils.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Result result = Result.fail(HttpStatus.UNAUTHORIZED.value(), "用户未获得认证");
        String jsonString = JSON.toJSONString(result);
        ResponseUtils.renderString(response,jsonString) ;
    }
}
