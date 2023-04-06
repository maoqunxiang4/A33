package com.fuchuang.A33.controller;

import com.fuchuang.A33.service.Impl.EmployeeRoleServiceImpl;
import com.fuchuang.A33.utils.UsualMethodUtils;
import com.fuchuang.A33.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employeeRole")
@Api(tags = "员工偏好")
public class EmployeeRoleController {
    @Autowired
    private EmployeeRoleServiceImpl employeeRoleService ;

    @PreAuthorize("hasAnyAuthority('group','view')")
    @GetMapping("/showEmployeeRole")
    @ApiOperation(value = "展示员工偏好")
    public Result showEmployeeRole(){
        return employeeRoleService.showEmployeeRole() ;
    }

    @PreAuthorize("hasAnyAuthority('group','view')")
    @GetMapping("/workTime")
    @ApiOperation(value = "展示所有可供选择的工作时间(新增)")
    public Result showWorkTime(){
        return employeeRoleService.showWorkTime() ;
    }

    @PreAuthorize("hasAnyAuthority('group','view')")
    @PostMapping("/updateWorkDay")
    @ApiOperation(value = "更改工作日偏好（最多添加两个）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workDayList", value = "工作日(一次只能传一个阿拉伯数字)" ,dataType= "Integer[]")
    })
    public Result updateWorkDay(Integer... workDayList ){
        return employeeRoleService.updateWorkDay(workDayList) ;
    }

    @PreAuthorize("hasAnyAuthority('group','view')")
    @PostMapping("/updateWorkTime")
    @ApiOperation(value = "更改工作时间偏好（最多添加两个）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workTimeList", value = "工作时间（格式要求，例：18:00~21:00）" ,dataType= "String[]")
    })
    public Result updateWorkTime(String... workTimeList ){
        return employeeRoleService.updateWorkTime(workTimeList) ;
    }

    @PreAuthorize("hasAnyAuthority('group','view')")
    @PostMapping("/updateLastTime")
    @ApiOperation(value = "更改工作时长偏好（最多添加一个）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "lastTime", value = "持续时长（以小时为基本单位）" ,dataType= "double")
    })
    public Result updateLastTime(double lastTime){
        return employeeRoleService.updateLastTime(lastTime) ;
    }


    //TODO 修改默认值
}
