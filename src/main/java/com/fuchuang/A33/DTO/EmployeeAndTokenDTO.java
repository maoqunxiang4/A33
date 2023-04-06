package com.fuchuang.A33.DTO;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAndTokenDTO {
    private String ID ;
    private String name ;
    private String position ;
    private String  shopID ;
    private String token ;
}
