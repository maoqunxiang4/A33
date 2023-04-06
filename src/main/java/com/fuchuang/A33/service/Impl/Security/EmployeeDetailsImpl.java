package com.fuchuang.A33.service.Impl.Security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuchuang.A33.entity.Authentication;
import com.fuchuang.A33.entity.Employee;
import com.fuchuang.A33.entity.LoginEmployee;
import com.fuchuang.A33.mapper.AuthenticationMapper;
import com.fuchuang.A33.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class EmployeeDetailsImpl implements UserDetailsService {
    @Autowired
    private EmployeeMapper employeeMapper ;

    @Autowired
    private AuthenticationMapper authenticationMapper ;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("email",username));
        if(Objects.isNull(employee)){
            return null ;
        }

        ArrayList<String> permissions = new ArrayList<>();
        ArrayList<Authentication> authentication
                = authenticationMapper.getAuthenticationsByPosition(employee.getID(), employee.getPosition());
        for (Authentication preAuthentication : authentication) {
            permissions.add(preAuthentication.getAuthentication()) ;
        }
        return new LoginEmployee(employee,permissions);
    }
}
