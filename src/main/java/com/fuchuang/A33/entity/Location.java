package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_locations")
public class Location {
    //日期+班次ID
    private String ID ;
    @TableField("current_number")
    private Integer currentNumber ;
    @TableField("permit_number")
    private Integer permitNumber ;
    @TableField("flow_ID")
    private String flowID ;
}
