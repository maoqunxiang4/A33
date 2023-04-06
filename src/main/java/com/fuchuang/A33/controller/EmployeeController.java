package com.fuchuang.A33.controller;

import com.fuchuang.A33.service.Impl.EmployeeServiceImpl;
import com.fuchuang.A33.service.Impl.LocationServiceImpl;
import com.fuchuang.A33.utils.EmployeeHolder;
import com.fuchuang.A33.utils.UsualMethodUtils;
import com.fuchuang.A33.utils.Result;
import com.fuchuang.A33.utils.ResultWithToken;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Api(tags = "员工接口")
public class EmployeeController {

    @Autowired
    private LocationServiceImpl locationService ;

    @Autowired
    private EmployeeServiceImpl employeeService ;

    @GetMapping("/login")
    @ApiOperation(value = "登陆（返回值是token，前端需要将token设置为全局变量，每次访问时请求头都需要携带token）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "员工邮箱" ,dataType= "String")
    })
    public Result login(String email){
        return employeeService.login(email) ;
    }

    @PostMapping("/regist")
    @ApiOperation(value = "注册(需要更改,新增员工手机号码)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "员工姓名" ,dataType= "String") ,
            @ApiImplicitParam(name = "email", value = "员工邮箱" ,dataType= "String") ,
            @ApiImplicitParam(name = "position", value = "员工职位" ,dataType= "String") ,
            @ApiImplicitParam(name = "shopID", value = "员工所属商店的ID号" ,dataType= "String") ,
            @ApiImplicitParam(name = "belong", value = "员工所属的组长的邮箱" ,dataType= "String") ,
            @ApiImplicitParam(name = "phone", value = "员工的手机号码" ,dataType= "String")
    })
    public Result regist(String name , String email ,String position ,String shopID ,String belong , String phone){
        return employeeService.regist(name ,email ,position ,shopID ,belong ,phone) ;
    }

    @PreAuthorize("hasAuthority('view')")
    @PutMapping("/group")
    @ApiOperation(value = "更改小组长")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupEmail", value = "小组长邮箱" ,dataType= "String" ) ,
    })
    public Result group(String groupEmail ){
        return employeeService.changeGroup(groupEmail) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/own")
    @ApiOperation(value = "展示个人信息(需要更改,新增员工手机号码)" )
    public ResultWithToken showOwnImformation(){
        return locationService.showEmployeeDetails(EmployeeHolder.getEmloyee().getID()) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @PutMapping("/updateOwn")
    @ApiOperation(value = "修改个人信息(需要更改,新增员工手机号码)（有修改，删除position字段）" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "员工邮箱" ,dataType= "String") ,
            @ApiImplicitParam(name = "phone", value = "员工手机号码" ,dataType= "String") ,
    })
    public Result updateOwnImformation(String email, String phone ){
        return employeeService.updateEmployeeInformation(email,phone ) ;
    }

    @PreAuthorize("hasAnyAuthority('boss','manage','group','view')")
    @GetMapping("/other")
    @ApiOperation(value = "(非root用户使用)展示本店铺其他人的信息" )
    public Result showOtherImformation(){
        return employeeService.showOtherImformation() ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @PostMapping("/updateOther")
    @ApiOperation(value = "高权限用户(boss)修改员工信息（有修改，新增name字段）" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "员工ID，前端选中后传入" ,dataType= "String") ,
            @ApiImplicitParam(name = "name", value = "员工姓名" ,dataType= "String") ,
            @ApiImplicitParam(name = "email", value = "员工邮箱" ,dataType= "String") ,
            @ApiImplicitParam(name = "position", value = "员工职位" ,dataType= "String") ,
            @ApiImplicitParam(name = "belong", value = "员工所属的组长的邮箱" ,dataType= "String") ,
            @ApiImplicitParam(name = "phone", value = "员工电话号码" ,dataType= "String") ,
    })
    public Result updateOtherImformation(String ID ,String name ,String email,String position , String belong, String phone){
        return employeeService.updateOtherImformation( UsualMethodUtils.parseID(ID) , name ,  email , position , belong , phone) ;
    }

    @PreAuthorize("hasAnyAuthority('boss')")
    @DeleteMapping("/remove")
    @ApiOperation(value = "移除员工信息(新增)" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "员工ID，前端选中后传入" ,dataType= "String")
    })
    public Result removeEmployee(String ID){
        return employeeService.removeEmployee( UsualMethodUtils.parseID(ID)) ;
    }

}
