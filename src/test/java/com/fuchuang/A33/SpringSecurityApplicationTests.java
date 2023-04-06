package com.fuchuang.A33;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuchuang.A33.entity.Employee;
import com.fuchuang.A33.mapper.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;

@SpringBootTest
class SpringSecurityApplicationTests {

    @Autowired
    private EmployeeMapper employeeMapper ;

    @Test
    void test1(){
    }

}
