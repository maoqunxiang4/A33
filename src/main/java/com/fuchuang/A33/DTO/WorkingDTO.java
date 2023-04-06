package com.fuchuang.A33.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkingDTO {
    //日期+班次ID
    private String employeeID ;
    private String shopID ;
    private String name ;
    private String position ;
    private String locationRealID ;
    private String locationID ;
}
