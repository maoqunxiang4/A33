package com.fuchuang.A33.service.Impl.Security;

import com.fuchuang.A33.mapper.EmployeeMapper;
import com.fuchuang.A33.mapper.ShopRoleMapper;
import com.fuchuang.A33.service.IScheduleService;
import com.fuchuang.A33.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleServiceImpl implements IScheduleService {
    @Autowired
    private ShopRoleMapper shopRoleMapper ;

    @Autowired
    private EmployeeMapper employeeMapper ;


    @Override
    public Result IntelligentSecheduling() {
        return null;
    }
}
