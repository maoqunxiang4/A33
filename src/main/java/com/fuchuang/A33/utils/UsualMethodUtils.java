package com.fuchuang.A33.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuchuang.A33.DTO.WorkingDTO;
import com.fuchuang.A33.entity.Employee;
import com.fuchuang.A33.entity.Location;
import com.fuchuang.A33.entity.Working;
import com.fuchuang.A33.mapper.LocationMapper;
import com.fuchuang.A33.mapper.WorkingMapper;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.fuchuang.A33.utils.Constants.*;
import static com.fuchuang.A33.utils.Constants.ALL_LOCATIONS;

public class UsualMethodUtils {

    public static LocalDateTime StringToChineseLocalDateTime(String date) {
        return LocalDateTime.of(Integer.parseInt(date.substring(0, 4)),
                        Integer.parseInt(date.substring(5, 7)), Integer.parseInt(date.substring(8, 10)), 0, 0)
                .atZone(ZoneId.of("Asia/Shanghai")).withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
    }

    public static LocalDateTime parseToMonday(String date) {
        LocalDateTime localDateTime = StringToChineseLocalDateTime(date);
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        while (dayOfWeek != DayOfWeek.MONDAY) {
            localDateTime = localDateTime.minusDays(1);
            dayOfWeek = localDateTime.getDayOfWeek();
        }
        return localDateTime;
    }

    public static LocalDateTime parseToMonday(LocalDateTime localDateTime) {
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        while (dayOfWeek != DayOfWeek.MONDAY) {
            localDateTime = localDateTime.minusDays(1);
            dayOfWeek = localDateTime.getDayOfWeek();
        }
        return localDateTime;
    }

    public static String parseID(String ID) {
        if (ID.contains("\"")) {
            String[] split = ID.split("\"");
            return split[1];
        }
        return ID;
    }

    public static String getRealFlowID(String locationID) {
        String ID;
        LocalDateTime week = StringToChineseLocalDateTime(locationID.substring(0, 10));
        int i;
        for (i = 0; i < 7; i++) {
            if (week.minusDays(i).getDayOfWeek() == DayOfWeek.MONDAY) break;
        }
        i = i * Constants.ONEDAY_COUNTS + Integer.parseInt(locationID.substring(11, 13)) - 1;
        if (i < 10) ID = "0" + i;
        else ID = String.valueOf(i);
        return ID;
    }

    public static void getGroupLocationListByWorkingList(List<Employee> employeeList
            , WorkingMapper workingMapper, LocationMapper locationMapper, String dateTime
            , ArrayList<WorkingDTO> workingDTOList1, ArrayList<List<WorkingDTO>> workingDTOList2
            , ArrayList<List<List<WorkingDTO>>> workingDTOList3) {

        LocalDateTime localDateTime = parseToMonday(dateTime);

        for (int i = 0; i < 7; i++) {
            //将天数进行转化
            String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String IDList = "";
            for (int j = 1; j <= ALL_LOCATIONS; j++) {
                if (j % (MIN_LAST_TIME / MIN_FLOW_TIME) != 0)
                    IDList = IDList + ("'" + date + "_" + (j < 10 ? "0" + j : j)) + "'" + ",";
                else IDList = IDList + ("'" + date + "_" + (j < 10 ? "0" + j : j) + "'");

                if (j % (MIN_LAST_TIME / MIN_FLOW_TIME) == 0 || j == ALL_LOCATIONS) {
                    if (j == ALL_LOCATIONS) IDList = IDList.substring(0, IDList.length() - 1);
                    List<Location> locations =
                            locationMapper.selectList(new QueryWrapper<Location>().inSql("ID", IDList));
                    if (!locations.isEmpty()) {
                        getWorkingDTOListByLocationList(workingMapper, locations, workingDTOList1, workingDTOList2);
                        for (List<WorkingDTO> workingDTOS : workingDTOList2) {
                            for (WorkingDTO workingDTO : workingDTOS) {
                                employeeList.removeIf(employee -> employee.getID().equals(workingDTO.getEmployeeID()));
                            }
                        }
                    } else {
                        workingDTOList2.add(workingDTOList1);
                    }
                    IDList = "";
                }
                workingDTOList1 = new ArrayList<>();

            }

            workingDTOList3.add(workingDTOList2);
            workingDTOList2 = new ArrayList<>();
            //天数加1
            localDateTime = localDateTime.plusDays(1);
        }

    }

    public static void getWorkingDTOListByLocationList(WorkingMapper workingMapper
            , List<Location> locationList, ArrayList<WorkingDTO> workingDTOList1
            , ArrayList<List<WorkingDTO>> workingDTOList2) {

        for (Location location : locationList) {
            List<Working> workingList = workingMapper.selectList(new QueryWrapper<Working>()
                    .eq("location_ID", location.getID()));
            if (workingList.size() != 0) {
                for (Working working : workingList) {
                    WorkingDTO workingDTO = new WorkingDTO();
                    //将结果转换成locationDTO对象
                    BeanUtil.copyProperties(working, workingDTO);
                    workingDTO.setLocationRealID(location.getID().substring(11));
                    workingDTOList1.add(workingDTO);
                }
            }
        }
        workingDTOList2.add(workingDTOList1);
    }

    public static void getSomeoneWorkingDTOListByLocationList(WorkingMapper workingMapper
            , List<Location> locationList, ArrayList<WorkingDTO> workingDTOList1
            , ArrayList<List<WorkingDTO>> workingDTOList2 ,String employeeID) {

        for (Location location : locationList) {
            List<Working> workingList = workingMapper.selectList(new QueryWrapper<Working>()
                    .eq("location_ID", location.getID()).eq("employee_ID",employeeID));
            if (workingList.size() != 0) {
                for (Working working : workingList) {
                    WorkingDTO workingDTO = new WorkingDTO();
                    //将结果转换成locationDTO对象
                    BeanUtil.copyProperties(working, workingDTO);
                    workingDTO.setLocationRealID(location.getID().substring(11));
                    workingDTOList1.add(workingDTO);
                }
            }
        }
        workingDTOList2.add(workingDTOList1);
    }


    public static void getOneDayAboutWorkingDTOListByLocationList(WorkingMapper workingMapper
            , ArrayList<WorkingDTO> workingDTOList1, ArrayList<List<WorkingDTO>> workingDTOList2
            , LocalDateTime localDateTime, LocationMapper locationMapper) {
        String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String IDList = "";
        for (int j = 1; j <= ALL_LOCATIONS; j++) {
            if (j % (MIN_LAST_TIME / MIN_FLOW_TIME) != 0)
                IDList = IDList + ("'" + date + "_" + (j < 10 ? "0" + j : j)) + "'" + ",";
            else IDList = IDList + ("'" + date + "_" + (j < 10 ? "0" + j : j) + "'");

            if (j % (MIN_LAST_TIME / MIN_FLOW_TIME) == 0 || j == ALL_LOCATIONS) {
                if (j == ALL_LOCATIONS) IDList = IDList.substring(0, IDList.length() - 1);
                List<Location> locations =
                        locationMapper.selectList(new QueryWrapper<Location>().inSql("ID", IDList));
                if (!locations.isEmpty()) {
                    getWorkingDTOListByLocationList(workingMapper, locations, workingDTOList1, workingDTOList2);
                } else {
                    workingDTOList2.add(workingDTOList1);
                }
                IDList = "";
            }
            workingDTOList1 = new ArrayList<>();

        }
    }


    public static void getSomeoneWorkingDTOListByLocationList(WorkingMapper workingMapper
            , ArrayList<WorkingDTO> workingDTOList1, ArrayList<List<WorkingDTO>> workingDTOList2
            , LocalDateTime localDateTime, LocationMapper locationMapper ,String employeeID) {

        String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String IDList = "";
        for (int j = 1; j <= ALL_LOCATIONS; j++) {
            if (j % (MIN_LAST_TIME / MIN_FLOW_TIME) != 0)
                IDList = IDList + ("'" + date + "_" + (j < 10 ? "0" + j : j)) + "'" + ",";
            else IDList = IDList + ("'" + date + "_" + (j < 10 ? "0" + j : j) + "'");

            if (j % (MIN_LAST_TIME / MIN_FLOW_TIME) == 0 || j == ALL_LOCATIONS) {
                if (j == ALL_LOCATIONS) IDList = IDList.substring(0, IDList.length() - 1);
                List<Location> locations =
                        locationMapper.selectList(new QueryWrapper<Location>()
                                .inSql("ID", IDList));
                if (!locations.isEmpty()) {
                    getSomeoneWorkingDTOListByLocationList(workingMapper, locations,
                            workingDTOList1, workingDTOList2, employeeID);
                } else {
                    workingDTOList2.add(workingDTOList1);
                }
                IDList = "";
            }
            workingDTOList1 = new ArrayList<>();

        }
    }

    public static ArrayList<String> parseLocation2Locations(String locationID){
        String locationRealID = locationID.substring(11, 13);
        String locationPrefix = locationID.substring(0, 11);
        //将locationRealID转换成数字
        int realID = Integer.parseInt(locationRealID) - 2;
        //创建存储locationRealID的集合
        ArrayList<String> realIDs = new ArrayList<>();
        //当locationRealID是4的倍数时，依次存取可以达到效果
        if (realID%(MIN_LAST_TIME/MIN_WORKINGTIME)==0){
            realIDs.add(convertToFormatID(realID+2)) ;
            realIDs.add(convertToFormatID(realID-1+2)) ;
            realIDs.add(convertToFormatID(realID-2+2)) ;
            realIDs.add(convertToFormatID(realID-3+2)) ;
        }else {
            //否则，就分两段进行存取
            int count = (int)(realID%(MIN_LAST_TIME/MIN_WORKINGTIME));
            int temp = realID ;
            while(count!=0){
                realIDs.add(convertToFormatID(String.valueOf(2+temp--))) ;
                count-- ;
            }
            count = (int)(realID%(MIN_LAST_TIME/MIN_WORKINGTIME));
            temp = realID+1 ;
            while(count!=4){
                realIDs.add(convertToFormatID(String.valueOf(2+temp++))) ;
                count ++ ;
            }
        }
        ArrayList<String> locationIDs = new ArrayList<>() ;
        for (String real : realIDs) {
            locationIDs.add(locationPrefix + real) ;
        }
        return locationIDs ;
    }

    public static String convertToFormatID (String ID){
        return Integer.parseInt(ID)<10 ? "0" + Integer.valueOf(ID) : ID ;
    }

    public static String convertToFormatID (Integer ID){
        return ID<10 ? "0" + ID : ID + "" ;
    }
}
