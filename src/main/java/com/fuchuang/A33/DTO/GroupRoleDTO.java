package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//分组规则
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRoleDTO {
    //当人数达到一定数量时就必须要一个小组长存在
    private Integer deadLineEmployee ;
}
