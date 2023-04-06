package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_employee_role")
public class EmployeeRole {
    @TableField("employee_ID")
    private String employeeID ;
    @TableField("hobby_type")
    private String hobbyType ;
    @TableField("hobby_value")
    private String hobbyValue ;
}
