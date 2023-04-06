package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenRoleDTO {
    //开店时间
    private double openTime ;
    //基数
    private double baseNum ;
    //门店总面积与基数之比
    private double fomula ;
}
