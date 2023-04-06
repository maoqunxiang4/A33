package com.fuchuang.A33.controller;

import com.fuchuang.A33.service.Impl.EmployeeServiceImpl;
import com.fuchuang.A33.service.Impl.ShopRoleServiceImpl;
import com.fuchuang.A33.utils.Result;
import com.fuchuang.A33.utils.UsualMethodUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/root")
@Api(tags = "root页面（暂时不需要写）")
public class RootController {
    @Autowired
    private ShopRoleServiceImpl shopRoleService ;

    @Autowired
    private EmployeeServiceImpl employeeService ;

    @PreAuthorize("hasAnyAuthority('root')")
    @PostMapping("/addShop")
    @ApiOperation(value = "添加商铺（root用户才能够操作）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "商铺名" ,dataType= "String") ,
            @ApiImplicitParam(name = "address", value = "商铺地址" ,dataType= "String") ,
            @ApiImplicitParam(name = "size" , value = "商铺面积" , dataType = "double") ,
    })
    public Result addShop(String name , String address , double size){
        return shopRoleService.addShop(name, address, size) ;
    }

    //TODO 移除店铺和店铺的规则

    @PreAuthorize("hasAnyAuthority('root')")
    @GetMapping("/rootForAll")
    @ApiOperation(value = "root展示店铺所有人的信息" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopID", value = "店铺ID" ,dataType= "String") ,
    })
    public Result showOtherImformation(String shopID){
        return employeeService.showEmployeeByRoot(UsualMethodUtils.parseID(shopID)) ;
    }

    @PreAuthorize("hasAnyAuthority('root')")
    @GetMapping("/allShop")
    @ApiOperation(value = "展示所有店铺（root用户专用页面）" )
    public Result showAllShop(){
        return employeeService.showAllShop() ;
    }

    @PreAuthorize("hasAnyAuthority('root')")
    @PutMapping("/updateOther")
    @ApiOperation(value = "高权限用户(root)修改员工信息" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "员工ID，前端选中后传入" ,dataType= "String") ,
            @ApiImplicitParam(name = "email", value = "员工邮箱" ,dataType= "String") ,
            @ApiImplicitParam(name = "position", value = "员工职位" ,dataType= "String") ,
            @ApiImplicitParam(name = "belong", value = "员工所属的组长的邮箱" ,dataType= "String") ,
            @ApiImplicitParam(name = "phone", value = "员工电话号码" ,dataType= "String") ,
    })
    public Result updateOtherImformation(String ID ,String name ,String email,String position , String belong, String phone){
        return employeeService.updateOtherImformation( UsualMethodUtils.parseID(ID) , name , email , position , belong , phone) ;
    }
}
