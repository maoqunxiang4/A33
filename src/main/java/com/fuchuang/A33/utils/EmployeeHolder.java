package com.fuchuang.A33.utils;

import com.fuchuang.A33.DTO.EmployeeDTO;
import com.fuchuang.A33.entity.Employee;

public class EmployeeHolder {
    private static final ThreadLocal<EmployeeDTO> tl = new ThreadLocal<>() ;

    public static void saveEmloyee(EmployeeDTO employee){
        tl.set(employee);
    }

    public static EmployeeDTO getEmloyee(){
        return tl.get() ;
    }

    public static void removeEmloyee(){
        tl.remove();
    }
}
