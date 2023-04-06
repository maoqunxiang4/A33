package com.fuchuang.A33.service;

import com.fuchuang.A33.utils.Result;
import org.springframework.stereotype.Repository;

@Repository
public interface IEmployeeRoleService {
    Result showEmployeeRole();
    Result updateWorkDay(Integer... workDayList);
    Result updateWorkTime(String... workTimeList);
    Result updateLastTime(double lastTime);
    Result showWorkTime();
}
