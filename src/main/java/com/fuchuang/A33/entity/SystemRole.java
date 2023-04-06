package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("t_system_role")
public class SystemRole {
    @TableField("shop_ID")
    private  String shopID ;
    @TableField("system_role_type")
    private  String systemRoleType ;
    @TableField("system_role_value")
    private  String systemRoleValue ;
    private  String comment ;
}
