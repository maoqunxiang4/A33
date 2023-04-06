package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//客流量规则
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowRoleDTO {
    //基数，每多少个客流就需要一个员工
    private double baseNum ;
}
