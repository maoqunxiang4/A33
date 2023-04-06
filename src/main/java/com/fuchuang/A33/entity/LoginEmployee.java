package com.fuchuang.A33.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
//创建一个LoginUser类，实现UserDetails接口，这个就是实现验证的关键
public class LoginEmployee implements UserDetails {

    private Employee employee;

    private List<String> permissions ;

    //这个地方要赋予空指针，因为构造方法里没有传参数给它
    private List<SimpleGrantedAuthority> simpleGrantedAuthorityList =  new ArrayList<>() ;

    public LoginEmployee(Employee employee, List<String> permissons) {
        this.employee = employee;
        this.permissions = permissons ;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //如果用户已经含有了权限，那么我们就不需要再授权了,至于后面的额外的授权操作，我们可以添加到其它
        if (!simpleGrantedAuthorityList.isEmpty()) return simpleGrantedAuthorityList ;
        //map的作用就是将我们的流对象转换成我们指定的对象
        simpleGrantedAuthorityList = permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return simpleGrantedAuthorityList;
    }

    @Override
    public String getPassword() {
        return new BCryptPasswordEncoder().encode("") ;
    }

    @Override
    public String getUsername() {
        return employee.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
