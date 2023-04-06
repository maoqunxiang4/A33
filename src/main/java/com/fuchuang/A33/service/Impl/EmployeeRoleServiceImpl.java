package com.fuchuang.A33.service.Impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuchuang.A33.DTO.*;
import com.fuchuang.A33.entity.EmployeeRole;
import com.fuchuang.A33.mapper.EmployeeRoleMapper;
import com.fuchuang.A33.mapper.ShopRoleMapper;
import com.fuchuang.A33.mapper.SystemRoleMapper;
import com.fuchuang.A33.service.IEmployeeRoleService;
import com.fuchuang.A33.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.fuchuang.A33.utils.Constants.*;

@Service
public class EmployeeRoleServiceImpl implements IEmployeeRoleService {

    @Autowired
    private EmployeeRoleMapper employeeRoleMapper ;

    @Autowired
    private ShopRoleMapper shopRoleMapper ;

    @Autowired
    private SystemRoleMapper systemRoleMapper ;

    @Override
    public Result showEmployeeRole() {
        String employeeID = EmployeeHolder.getEmloyee().getID();
        List<EmployeeRole> employeeRoleList =
                employeeRoleMapper.selectList(new QueryWrapper<EmployeeRole>().eq("employee_ID", employeeID));

        roleIsEmplty(employeeID,employeeRoleList);

        EmployeeRoleDTO employeeRoleDTO = new EmployeeRoleDTO();
        setAllEmployeeRoleValue(employeeRoleList ,employeeRoleDTO ,shopRoleMapper ,systemRoleMapper
                , employeeRoleMapper);
        employeeRoleDTO.setID(employeeID);
        return Result.success(200,employeeRoleDTO);
    }

    @Override
    public Result updateWorkDay(Integer... workDayList) {
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);
        EmployeeDTO employee = EmployeeHolder.getEmloyee();

        EmployeeWorkDayDTO employeeWorkDayDTO = new EmployeeWorkDayDTO();
        ArrayList<Integer> workDayArrayList = new ArrayList<>(Arrays.asList(workDayList));
        employeeWorkDayDTO.setEmployeeWorkDayList(workDayArrayList);
        roleUtils.saveEmployeeRole(EmployeeWorkDayDTO.class , employeeWorkDayDTO , EMPLOYEEROLE_TYPE1 , employee.getID()) ;

        return Result.success(200);
    }

    @Override
    public Result updateWorkTime(String... workTimeList) {
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);
        EmployeeDTO employee = EmployeeHolder.getEmloyee();

        ArrayList<String> workTimeArrayList = new ArrayList<>(Arrays.asList(workTimeList));
        EmployeeWorkTimeDTO employeeWorkTimeDTO = new EmployeeWorkTimeDTO();

        employeeWorkTimeDTO.setEmployeeWorkTimeList(workTimeArrayList);
        roleUtils.saveEmployeeRole(EmployeeWorkTimeDTO.class , employeeWorkTimeDTO, EMPLOYEEROLE_TYPE2 , employee.getID()) ;

        return Result.success(200);
    }

    @Override
    public Result updateLastTime(double lastTime) {
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);
        EmployeeDTO employee = EmployeeHolder.getEmloyee();
        EmployeeLastTimeDTO employeeLastTimeDTO = new EmployeeLastTimeDTO();
        employeeLastTimeDTO.setLastTime(lastTime);
        roleUtils.saveEmployeeRole(EmployeeLastTimeDTO.class , employeeLastTimeDTO, EMPLOYEEROLE_TYPE3 , employee.getID()) ;

        return Result.success(200);
    }

    @Override
    public Result showWorkTime() {
        int time = 9 ;
        ArrayList<String> times = new ArrayList<>();
        while(time<21){
            String allTime = "" ;
            allTime += UsualMethodUtils.convertToFormatID(time) + ":00~" ;
            time += 2 ;
            allTime += UsualMethodUtils.convertToFormatID(time) + ":00" ;
            times.add(allTime);
        }
        return Result.success(200,times);
    }

    public void setAllEmployeeRoleValue(List<EmployeeRole> employeeRoles , EmployeeRoleDTO employeeRoleAllDTO ,
                                        ShopRoleMapper shopRoleMapper , SystemRoleMapper systemRoleMapper ,
                                        EmployeeRoleMapper employeeRoleMapper){
        EmployeeDTO employee = EmployeeHolder.getEmloyee();
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);

        for (EmployeeRole employeeRole : employeeRoles) {
            switch (employeeRole.getHobbyType()){
                case Constants.EMPLOYEEROLE_TYPE1 : {
                    if (employeeRole.getHobbyValue() == null) employeeRoleAllDTO.setEmployeeWorkDayDTO(null);
                    else employeeRoleAllDTO
                            .setEmployeeWorkDayDTO(roleUtils.parseEmployeeRole(EmployeeWorkDayDTO.class ,EMPLOYEEROLE_TYPE1
                                    , employee.getID()));
                    break ;
                }
                case Constants.EMPLOYEEROLE_TYPE2 : {
                    if (employeeRole.getHobbyValue() == null) employeeRoleAllDTO.setEmployeeWorkTimeDTO(null);
                    else employeeRoleAllDTO
                            .setEmployeeWorkTimeDTO(roleUtils.parseEmployeeRole(EmployeeWorkTimeDTO.class ,EMPLOYEEROLE_TYPE2
                                    , employee.getID()));
                    break;
                }
                case Constants.EMPLOYEEROLE_TYPE3 : {
                    if (employeeRole.getHobbyValue() == null) employeeRoleAllDTO.setEmployeeLastTimeDTO(null);
                    else employeeRoleAllDTO
                            .setEmployeeLastTimeDTO(roleUtils.parseEmployeeRole(EmployeeLastTimeDTO.class ,EMPLOYEEROLE_TYPE3
                                    , employee.getID()));
                    break;
                }
            }
        }
    }

    public void roleIsEmplty(String employeeID ,List<EmployeeRole> employeeRoleList){
        if (employeeRoleList.isEmpty()){
            EmployeeRole employeeRole = new EmployeeRole(employeeID , EMPLOYEEROLE_TYPE1 , null );
            employeeRoleMapper.insert(employeeRole) ;

            employeeRole = new EmployeeRole(employeeID , EMPLOYEEROLE_TYPE2 , null );
            employeeRoleMapper.insert(employeeRole) ;

            employeeRole = new EmployeeRole(employeeID , EMPLOYEEROLE_TYPE3 , null );
            employeeRoleMapper.insert(employeeRole) ;
        }
    }
}
