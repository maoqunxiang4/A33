package com.fuchuang.A33.controller;

import com.fuchuang.A33.service.Impl.ShopRoleServiceImpl;
import com.fuchuang.A33.utils.UsualMethodUtils;
import com.fuchuang.A33.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shopRole")
@Api(tags = "商铺及商铺规则")
public class ShopRoleController {
    @Autowired
    private ShopRoleServiceImpl shopRoleService ;

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateFlow")
    @ApiOperation(value = "修改用户自定义规则之客流量规则")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "baseNum", value = "基础值" ,dataType= "double") ,
    })
    public Result updateFlowRole( double baseNum){
        return shopRoleService.updateFlowRole( baseNum) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateCloseRole")
    @ApiOperation(value = "修改用户自定义规则之关店规则")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "endTime", value = "关店时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "minEmployee", value = "最小员工数" ,dataType= "int") ,
            @ApiImplicitParam(name = "baseNum", value = "基础值" ,dataType= "double") ,
            @ApiImplicitParam(name = "fomula", value = "门店总面积与基础值之比" ,dataType= "double")

    })
    public Result updateCloseRole( double endTime , Integer minEmployee , double baseNum ,double fomula ){
        return shopRoleService.updateCloseRole(endTime , minEmployee , baseNum , fomula) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateOpenRole")
    @ApiOperation(value = "修改用户自定义规则之开店规则")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "openTime", value = "开店时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "baseNum", value = "基础值" ,dataType= "double") ,
            @ApiImplicitParam(name = "fomula", value = "门店总面积与基础值之比" ,dataType= "double")

    })
    public Result updateOpenRole( double openTime , double baseNum ,double fomula ){
        return shopRoleService.updateOpenRole(openTime , baseNum , fomula) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateGroupRole")
    @ApiOperation(value = "修改用户自定义规则之分组规则")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deadLineEmployee", value = "每n个员工配置一个小组长" ,dataType= "int")
    })
    public Result updateGroupRole(int deadLineEmployee){
        return shopRoleService.updateGroupRole(deadLineEmployee) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateStockRole")
    @ApiOperation(value = "修改用户自定义规则之进货规则")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "minEmployee", value = "最小员工数" ,dataType= "int") ,
            @ApiImplicitParam(name = "minLastTime", value = "进货最短持续时长" ,dataType= "double") ,
            @ApiImplicitParam(name = "maxLastTime", value = "进货最长持续时长" ,dataType= "double")

    })
    public Result updateStockRole(int minEmployee , double minLastTime , double maxLastTime){
        return shopRoleService.updateStockRole(  minEmployee , minLastTime ,maxLastTime) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateLastTime")
    @ApiOperation(value = "修改系统规则之持续时长")
    @ApiImplicitParams({
            //TODO 更改一下workDay的注释
            @ApiImplicitParam(name = "workDay", value = "工作日(例1,2,3,4,5)" ,dataType= "String") ,
            @ApiImplicitParam(name = "workStart", value = "工作日工作开始时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "workEnd", value = "工作日工作结束时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "weekend", value = "周末(例子6,7)" ,dataType= "String") ,
            @ApiImplicitParam(name = "weekendStart", value = "周末工作开始时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "weekendEnd", value = "周末工作结束时间" ,dataType= "double")
    })
    public Result updateLastTime( String workDay ,double workStart ,
                                  double workEnd ,String weekend ,double weekendStart , double weekendEnd ){
        return shopRoleService.updateLastTime( workDay , workStart ,
                 workEnd , weekend , weekendStart ,  weekendEnd ) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateRestTime")
    @ApiOperation(value = "修改系统规则之休息时长")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "lunchTime", value = "午餐时间长短" ,dataType= "double") ,
            @ApiImplicitParam(name = "lunchTimeStart", value = "午餐时间开始值" ,dataType= "double") ,
            @ApiImplicitParam(name = "lunchTimeEnd", value = "午餐时间结束值" ,dataType= "double") ,
            @ApiImplicitParam(name = "dinnerTime", value = "晚饭时间长短" ,dataType= "double") ,
            @ApiImplicitParam(name = "dinnerTimeStart", value = "晚饭时间开始值" ,dataType= "double") ,
            @ApiImplicitParam(name = "dinnerTimeEnd", value = "晚饭时间结束值" ,dataType= "double") ,
            @ApiImplicitParam(name = "restTime", value = "休息时间长短" ,dataType= "double")
    })
    public Result updateRestTime(  double lunchTime , double lunchTimeStart , double lunchTimeEnd , double dinnerTime ,
                                   double dinnerTimeStart , double dinnerTimeEnd , double restTime ){
        return shopRoleService.updateRestTime( lunchTime , lunchTimeStart , lunchTimeEnd , dinnerTime ,
                 dinnerTimeStart ,  dinnerTimeEnd ,  restTime) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateWorkTime")
    @ApiOperation(value = "修改系统规则之工作时长")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "weekMaxTime", value = "一周最长工作时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "dayMaxTime", value = "一天最长工作时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "locationMinTime", value = "单次轮班最短时间" ,dataType= "double") ,
            @ApiImplicitParam(name = "locationMaxTime", value = "单次轮班最长时间" ,dataType= "double")
    })
    public Result updateWorkTime(  double weekMaxTime ,double dayMaxTime ,double locationMinTime ,double locationMaxTime ){
        return shopRoleService.updateWorkTime( weekMaxTime , dayMaxTime , locationMinTime , locationMaxTime) ;
    }

    //TODO
    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateGroupPosition")
    @ApiOperation(value = "修改系统规则之班次职位数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "number", value = "指的是各个职位的人数，与下面的职位一一对应(格式，例：1,1,1)" ,dataType= "String") ,
            @ApiImplicitParam(name = "position", value = "职位，与number一一对应(如果拼接导购，收银，库房，则表示一个导购,一个收银,一个库房)" ,dataType= "String")
    })
    public Result updateGroupPosition(String number , String... position ){
        return shopRoleService.updateGroupPosition(number , position) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/showSystem")
    @ApiOperation(value = "展示系统规则(新增)")
    public Result showSystemRole(){
        return shopRoleService.showSystemRole() ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/showDiy")
    @ApiOperation(value = "展示用户自定义的规则值(新增)")
    public Result showShopRoleAndShopValue(){
        return shopRoleService.showShopRoleAndShopValue() ;
    }

}
