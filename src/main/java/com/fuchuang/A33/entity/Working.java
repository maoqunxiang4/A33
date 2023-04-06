package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_working")
public class Working {
    @TableField("employee_ID")
    private String employeeID ;
    @TableField("shop_ID")
    private String shopID ;
    private String name ;
    private String position ;
    @TableField("Location_ID")
    private String locationID ;
}
