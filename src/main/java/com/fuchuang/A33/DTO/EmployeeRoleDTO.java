package com.fuchuang.A33.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import static com.fuchuang.A33.utils.Constants.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeeRoleDTO {
    private String ID ;
    private final String hobbyType1 = EMPLOYEEROLE_TYPE1 ;
    private EmployeeWorkDayDTO employeeWorkDayDTO ;
    private final String hobbyType2 = EMPLOYEEROLE_TYPE2 ;
    private EmployeeWorkTimeDTO employeeWorkTimeDTO ;
    private final String hobbyType3 = EMPLOYEEROLE_TYPE3 ;
    private EmployeeLastTimeDTO employeeLastTimeDTO ;
}
