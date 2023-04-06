package com.fuchuang.A33.filter;

import cn.hutool.core.bean.BeanUtil;
import com.fuchuang.A33.DTO.EmployeeDTO;
import com.fuchuang.A33.entity.Employee;
import com.fuchuang.A33.entity.LoginEmployee;
import com.fuchuang.A33.utils.Constants;
import com.fuchuang.A33.utils.EmployeeHolder;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate ;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)){
            filterChain.doFilter(request,response);
            return;
        }
        token = Constants.EMPLOYEE_TOKEN + token ;
        Map<Object, Object> authentication = stringRedisTemplate.opsForHash().entries(token);
        EmployeeDTO employeeDTO = BeanUtil.fillBeanWithMap(authentication, new EmployeeDTO(), true);
        if(authentication.isEmpty()){
            throw new RuntimeException("can not find the user now") ;
        }
        Employee employee = new Employee();
        BeanUtil.copyProperties(employeeDTO,employee,false);
        LoginEmployee loginEmployee = new LoginEmployee(employee,employeeDTO.getPermissions());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authentication, null, loginEmployee.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        stringRedisTemplate.expire(token,60*30, TimeUnit.SECONDS) ;
        EmployeeHolder.saveEmloyee(employeeDTO);
        filterChain.doFilter(request,response);
    }
}
