package com.fuchuang.A33.service;

import com.fuchuang.A33.utils.Result;
import org.springframework.stereotype.Repository;

@Repository
public interface IShopRoleService {
    Result addShop(String name, String address, double size);
    Result showSystemRole() ;
    Result showShopRoleAndShopValue() ;
    Result updateFlowRole(double baseNum);
    Result updateCloseRole(double endTime, Integer minEmployee, double baseNum, double fomula);
    Result updateOpenRole(double openTime, double baseNum, double fomula) ;
    Result updateGroupRole(int deadLineEmployee);
    Result updateStockRole(int minEmployee, double minLastTime, double maxLastTime);
    Result updateLastTime(String workDay, double workStart, double workEnd,
                          String weekend, double weekendStart, double weekendEnd);
    Result updateRestTime(double lunchTime, double lunchTimeStart, double lunchTimeEnd,
                          double dinnerTime, double dinnerTimeStart, double dinnerTimeEnd, double restTime);
    Result updateWorkTime(double weekMaxTime, double dayMaxTime, double locationMinTime, double locationMaxTime);
    Result updateGroupPosition(String number, String[] position);
}
