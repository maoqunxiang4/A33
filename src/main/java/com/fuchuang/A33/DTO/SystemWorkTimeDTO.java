package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SystemWorkTimeDTO {
    private double weekMaxTime ;
    private double dayMaxTime ;
    private double locationMinTime ;
    private double locationMaxTime ;
}
