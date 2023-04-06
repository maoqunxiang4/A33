package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("t_priority")
public class Priority {
    @TableField("employee_ID")
    private String employeeID ;
    private double priority ;
}