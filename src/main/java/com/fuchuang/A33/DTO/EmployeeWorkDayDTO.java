package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

//TODO 修改一下员工偏好的逻辑
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeWorkDayDTO {
    private ArrayList<Integer> employeeWorkDayList ;
}
