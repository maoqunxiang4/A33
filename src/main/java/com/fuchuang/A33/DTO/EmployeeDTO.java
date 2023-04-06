package com.fuchuang.A33.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private String name ;
    private String position ;
    @TableField("shop_ID")
    private String  shopID ;
    @TableField(exist = false)
    private List<String> permissions ;
    private String ID ;
}
