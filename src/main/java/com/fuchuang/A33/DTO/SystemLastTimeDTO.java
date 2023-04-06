package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SystemLastTimeDTO {
    private String workDay ;
    private double workStart ;
    private double workEnd ;
    private String weekend ;
    private double weekendStart ;
    private double weekendEnd ;
}
