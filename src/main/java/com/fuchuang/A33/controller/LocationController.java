package com.fuchuang.A33.controller;

import com.fuchuang.A33.service.Impl.LocationServiceImpl;
import com.fuchuang.A33.utils.UsualMethodUtils;
import com.fuchuang.A33.utils.Result;
import com.fuchuang.A33.utils.ResultWithToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
@Api(tags = "员工班次")
//TODO 更改展示排班的逻辑
public class LocationController {
    @Autowired
    private LocationServiceImpl locationService ;

    @GetMapping("/threeMonthes")
    @ApiOperation(value = "获取前三个月和后三个月的具体时间")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dateTime", value = "当前时间" ,dataType= "String")
    })
    public Result getThreeMonthes(String dateTime){
        return locationService.getThreeMonthes(dateTime) ;
    }


    @GetMapping("/monday")
    @ApiOperation(value = "获取本周星期一的日期")
    public Result getMondayThisWeek(){
        return locationService.getMondayThisWeek() ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group')")
    @GetMapping("/week")
    @ApiOperation(value = "按周展示员工的工作安排")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dateTimeWeek", value = "选中的周" ,dataType= "String")
    })
    public Result showAllLocationsByWeek(String dateTimeWeek){
        return locationService.showAllLocationsByWeek(dateTimeWeek) ;
    }


    @PreAuthorize("hasAnyAuthority('boss','manage','group')")
    @GetMapping("/day")
    @ApiOperation(value = "按日展示员工的工作安排")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dateTimeDay", value = "选中的日期" ,dataType= "String") ,
    })
    public Result showAllLocationsByDay(String dateTimeDay){
        return locationService.showAllLocationsByDay(dateTimeDay) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage')")
    @GetMapping("/allGroup")
    @ApiOperation(value = "展示所有的小组长和及其组员(原：展示所有组。现为了表达更准确换一个表达方式)")
    public Result showAllGroup(){
        return locationService.showAllGroup() ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage')")
    @GetMapping("/position")
    @ApiOperation(value = "按职位展示排班(新增)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "position", value = "职位" ,dataType= "String") ,
            @ApiImplicitParam(name = "dateTime", value = "当前时间" ,dataType= "String") ,
    })
    public Result showEmployeesByPosition(String position , String dateTime){
        return locationService.showEmployeeLocationsByPosition(position,dateTime) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group')")
    @GetMapping("/group")
    @ApiOperation(value = "按组展示员工的排班信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupID", value = "选中的组别，将选中的小组长的ID信息传入" ,dataType= "String") ,
            @ApiImplicitParam(name = "dateTime", value = "当前时间" ,dataType= "String")
    })
    public Result showAllLocationsByGroup(String groupID ,String dateTime){
        return locationService.showAllLocationsByGroup(UsualMethodUtils.parseID(groupID) , dateTime) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group')")
    @GetMapping("/details")
    @ApiOperation(value = "展示员工的具体信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "employeeID", value = "员工ID号" ,dataType= "String") ,
    })
    public ResultWithToken showEmployeeDetails(String employeeID){
        return locationService.showEmployeeDetails(UsualMethodUtils.parseID(employeeID)) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage')")
    @PutMapping("/manage")
    @ApiOperation(value = "手动安排员工班次（新增：可多选）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "locationID", value = "值班号" ,dataType= "String[]") ,
            @ApiImplicitParam(name = "employeeID", value = "员工ID号" ,dataType= "String")
    })
    public Result manageEmployeeLocationsByHand( String employeeID,String... locationID){
        return locationService.manageEmployeeLocationsByHand( employeeID, locationID) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage')")
    @DeleteMapping("/remove")
    @ApiOperation(value = "手动移除员工班次（新增：可多选）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "locationID", value = "值班号" ,dataType= "String[]") ,
            @ApiImplicitParam(name = "employeeID", value = "员工ID号" ,dataType= "String")
    })
    public Result removeLocationsByHand( String employeeID ,String... locationID ){
        return locationService.removeLocationsByHand(  employeeID , locationID) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group')")
    @GetMapping("/name")
    @ApiOperation(value = "（搜索时使用）通过员工姓名展示员工（与/A33/location/freeEmployees搭配使使用）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "姓名" ,dataType= "String")
    })
    public Result showEmployeeByName( String name){
        return locationService.showEmployeeByName( name) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group')")
    @GetMapping("/selectLocations")
    @ApiOperation(value = "搭配/A33/location/name接口使用，上述接口选中传入email信息，用于展示所搜索的员工的所有的排班信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dateTime", value = "日期" ,dataType= "String") ,
            @ApiImplicitParam(name = "email", value = "员工邮箱" ,dataType= "String")
    })
    public Result showEmployeeLocationsByEmail(String dateTime ,String email){
        return locationService.showEmployeeLocationsByEmail(dateTime , email) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group')")
    @GetMapping("/showFree")
    @ApiOperation(value = "展示所有的空闲员工")
    public Result showFreeEmployees(){
        return locationService.showFreeEmployees() ;
    }


    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/positions")
    @ApiOperation(value = "获取所有职位" )
    public Result getPositions(){
        return locationService.getPositions();
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/locationDetails")
    @ApiOperation(value = "展示选中班次的具体员工" )
    public Result showLocationDetails(String locationID){
        return locationService.showLocationDetails(locationID);
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/one/weekLocations")
    @ApiOperation(value = "获取当前人员某一周的班次" )
    public Result showWeekLocationsOfOne(String dateTimeWeek){
        return locationService.showWeekLocationOfOne(dateTimeWeek);
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/one/dayLocations")
    @ApiOperation(value = "获取当前人员某一天的班次" )
    public Result showDayLocationsOfOne(String dateTimeDay){
        return locationService.showDayLocationOfOne(dateTimeDay);
    }
}
