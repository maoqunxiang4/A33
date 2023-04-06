package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//进货规则
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockRoleDTO {
    //最小员工数
    private Integer minEmployee ;
    //每一次进货的最短持续时长
    private double minLastTime ;
    //每一次进货的最长持续时长
    private double maxLastTime ;
}
