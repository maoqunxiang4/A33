package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_shop_role")
public class ShopRole {
    @TableField("shop_ID")
    private String shopID ;
    @TableField("shop_role_type")
    private String shopRoleType ;
    @TableField("shop_role_value")
    private String shopRoleValue ;
    private String comment;
}
