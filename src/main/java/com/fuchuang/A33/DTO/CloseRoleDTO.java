package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloseRoleDTO {
    //关店时间
    private double endTime ;
    //最小员工数
    private Integer minEmployee ;
    //基数
    private double baseNum ;
    //门店总面积与基数之比
    private double fomula ;
}
