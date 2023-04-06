package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_times")
public class Times {
    @TableField("employee_ID")
    private String employeeID ;
    @TableField("time_Sum")
    private double timeSum ;
    private Integer counts ;
    @TableField("permit_time")
    private double permitTime ;
}