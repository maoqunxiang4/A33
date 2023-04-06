package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeInformationDTO {
    private String name ;
    private String email ;
    private String position ;
    private String shopName ;
    private String groupName ;
}
