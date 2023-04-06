package com.fuchuang.A33.service;

import com.fuchuang.A33.utils.Result;
import com.fuchuang.A33.utils.ResultWithToken;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ILocationService {
    Result getThreeMonthes(String dateTimeWeek) ;
    Result getMondayThisWeek() ;
    Result showAllLocationsByWeek(String dateTimeDay);
    Result showAllLocationsByDay(String dateTimeDay);
    Result showAllGroup () ;
    Result showAllLocationsByGroup(String groupID , String dateTime);
    ResultWithToken showEmployeeDetails(String employeeID) ;
    Result manageEmployeeLocationsByHand( String LocationID , String... locationIDList) ;
    Result removeLocationsByHand( String employeeID , String... LocationIDList) ;
    Result showEmployeeByName( String name) ;
    Result showEmployeeLocationsByEmail(String dateTime ,String email) ;
    Result showFreeEmployees() ;
    Result showEmployeeLocationsByPosition(String position , String dateTime);
    Result getPositions();
    Result showLocationDetails(String locationID) ;
    Result showWeekLocationOfOne(String dateTimeWeek) ;
    Result showDayLocationOfOne(String dateTimeDay) ;
}
