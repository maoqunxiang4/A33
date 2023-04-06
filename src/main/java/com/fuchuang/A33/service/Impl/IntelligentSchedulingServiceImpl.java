package com.fuchuang.A33.service.Impl;

import cn.hutool.core.lang.hash.Hash;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fuchuang.A33.DTO.*;
import com.fuchuang.A33.entity.*;
import com.fuchuang.A33.mapper.*;
import com.fuchuang.A33.utils.EmployeeHolder;
import com.fuchuang.A33.utils.RoleUtils;
import com.fuchuang.A33.utils.UsualMethodUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.fuchuang.A33.utils.Constants.*;

@Service
public class IntelligentSchedulingServiceImpl {

    @Autowired
    private WorkingMapper workingMapper;

    @Autowired
    private ShopRoleMapper shopRoleMapper;

    @Autowired
    private SystemRoleMapper systemRoleMapper;

    @Autowired
    private EmployeeRoleMapper employeeRoleMapper;

    @Autowired
    private FlowMapper flowMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private TimesMapper timesMapper;

    @Autowired
    private PriorityMapper priorityMapper;

    //TODO 进货规则，开关店的整理规则;早晚班额外委派的人员
    //TODO 数据结构最好还是写成HashMap结构
    //TODO 工作时间是数组还是字符串?
    //TODO 对安排早班的人进行限制
    public void IntelligentSchedulingAlgorithm(LocalDateTime localDateTime) {
        String shopID = EmployeeHolder.getEmloyee().getShopID();
        /*
        获取所有的规则值
         */
        AllRoleDTO allRoleDTO = new AllRoleDTO(shopID, shopRoleMapper, systemRoleMapper);

        /*
        获取每个时间段的人数
         */
        ArrayList<EmployeeNumberDTO> employeeNumberDTOList
                = getPeopleNumInEveryLocation(allRoleDTO.getFlowRoleDTO(), localDateTime,allRoleDTO);

        //TODO 设置一个浮动区间
        /*
        获取每个员工的偏好
         */
        ArrayList<EmployeeRoleDTO> employeeRoleDTOList = getEmployeeRoleArrayList(shopID);

        /*
        获取员工优先值信息
         */
        ArrayList<PriorityDTO> priorityDTOList = getInitOneShopPriorityDTOs(shopID);
        getPirority(priorityDTOList);
        prioritySort(priorityDTOList);

        //获取员工数据
        List<Employee> employees = getEmployees(shopID);
        ArrayList<Employee> allEmployee = new ArrayList<>(employees);
        //删除此前的所有记录
        deleteAllLocations(localDateTime, employees);
        //获取时间数据
        ArrayList<Times> timesArrayList = getEmployeeTimes();
        //获取boss和manage
        ArrayList<Employee> manageAndBoss = getManageAndBoss(employees);

        /*根据各个班次的人数，优先对小组长进行排班，小组长所带的队员优先进行排班，小组长所带的人必须是齐全的，并且人数就是客流量最大的时候对应的数值
         *对小组长进行排班，与此同时，优先对小组长所喜欢的日子进行排序，当人数过多时，选中的小组优先级不变，未选中的小组优先值+1
         */
        //TODO 小组长下面没人会报错,更改一下小组长的排班逻辑，改写成小组长轮流值一天班
        manageGroup(priorityDTOList, employees, employeeRoleDTOList
                , allRoleDTO, employeeNumberDTOList, localDateTime, timesArrayList);
        prioritySort(priorityDTOList);

        manageOtherGroup(priorityDTOList, employees, employeeRoleDTOList, allRoleDTO
                , employeeNumberDTOList, timesArrayList);
        prioritySort(priorityDTOList);

        removeEmployee(priorityDTOList, employees);
        /*
         *特殊情况，如果这个人愿意在非工作日工作，优先级加3，不愿意在非工作日工作但是最后到非工作日工作，则优先级加4，其他人优先值减1
         * */
        manageWeekDayForEmployee(priorityDTOList, employeeRoleDTOList, employeeNumberDTOList
                , localDateTime, allRoleDTO, timesArrayList);
        prioritySort(priorityDTOList);

        /*先排序所有的员工，每个员工都会值一次早班，值一次晚班。因为早晚班属于员工的意愿，所以这一段我们就直接进行排班，对于愿意值早晚班的，我们直接根据他喜欢的
         *日子直接进行排班，且优先值不变。如果同一个时间段人数过多，根据营业规则，选择一部分人进行排班，剩下的人优先值加一对于这一部分人，安排到其它
         *的早晚班时间段内进行排班，剩下的没有拍到班的，优先值加一
         */
        //TODO 对早晚班员工添加工作时长
        manageMorningAndNightLocationForEmployee(priorityDTOList, employeeRoleDTOList, employeeNumberDTOList
                , localDateTime, allRoleDTO, timesArrayList);
        prioritySort(priorityDTOList);

        /*
        按照同样的规则，对完全处于午餐和晚餐状态的员工进行排班，排班的方式和排早班一致
         */
        // TODO 进行修改
        manageLunchAndDinnerLocationForEmployee(priorityDTOList, employeeRoleDTOList,
                employeeNumberDTOList, localDateTime, allRoleDTO, timesArrayList);
        prioritySort(priorityDTOList);

        /*
         * 此时将所有的位置的人数进行一遍审核，得出人数的差值。将优先级高的人进行安排在黄金时间点，优先值减一；优先级低的就安排在早晚班剩下的岗位、进货时间点、
         * 人流量大的时间点，优先值加一，每当一个员工安排在了工作日黄金时间段，那么就将员工的优先级就减2
         */
        manageBestTimeLocationForEmployee(priorityDTOList, employeeNumberDTOList, localDateTime
                , allRoleDTO.getSystemWorkTimeDTO(), timesArrayList, allRoleDTO.getSystemRestTimeDTO());
        prioritySort(priorityDTOList);

        /*
        按照优先级对所有人进行排班，按照他们的意愿进行安排，插入成功后优先级减少1
         */
        manageAllEmployeesByEmployeeRole(priorityDTOList, employeeRoleDTOList, employeeNumberDTOList
                , localDateTime, allRoleDTO.getSystemWorkTimeDTO(), allRoleDTO.getSystemRestTimeDTO(), timesArrayList);
        prioritySort(priorityDTOList);

        /*扫描全部的location，对时间充裕的员工进行排班操作，对于几个特定的时间点，
        优先值可以进行增加，不需要进行排序，如早晚+1，中午+2，非工作日早晚+2，非工作日中午+3
        */
        manageEmployeeForOtherLocation(priorityDTOList, employeeNumberDTOList, localDateTime, employees
                , allRoleDTO.getSystemWorkTimeDTO(), allRoleDTO.getSystemRestTimeDTO(), timesArrayList);

        prioritySort(priorityDTOList);
        //防止优先级很低的用户选不到好的班次，对这种情况进行限制
        /*
        *进货规则,安排进货员工，在每周1，5晚上的20点进行排班，且经理和店长必须有一个在场，与关门前的排班相映衬
         */
        manageStockAndCloseEmployee(priorityDTOList, employeeNumberDTOList, allRoleDTO.getSystemWorkTimeDTO(),
                allRoleDTO.getSystemRestTimeDTO(), timesArrayList, manageAndBoss ,allEmployee);

        /*
        对时长未达标的员工进行排班,优先排班到非工作日
         */
        manageFreeEmployee(priorityDTOList, employeeNumberDTOList, localDateTime
                , allRoleDTO.getSystemWorkTimeDTO(), allRoleDTO.getSystemRestTimeDTO(), timesArrayList);


        updateEmployeeTimesByJDBC(timesArrayList);
        /*
         * 更新优先级
         */
        updatePriority(priorityDTOList);
    }

    /**
     * 安排人员在进行进货排班
     */
    private void manageStockAndCloseEmployee(ArrayList<PriorityDTO> priorityDTOList, ArrayList<EmployeeNumberDTO> employeeNumberDTOList,
                                              SystemWorkTimeDTO systemWorkTimeDTO, SystemRestTimeDTO systemRestTimeDTO,
                                             ArrayList<Times> timesArrayList,ArrayList<Employee> manageAndBoss,ArrayList<Employee> employees ) {

        ArrayList<String> employeeIDs = getFreeEmployeeIDs(timesArrayList);
        ArrayList<String> removeIDs = new ArrayList<>();
        for (String employeeID : employeeIDs) {
            for (Employee manageOrBoss : manageAndBoss) {
                if (manageOrBoss.getID().equals(employeeID)) {
                    removeIDs.add(employeeID) ;
                }
            }
        }

        for (Employee employee : employees) {
            if (employee.getPosition().equals("小组长")){
                employeeIDs.remove(employee.getID()) ;
            }
        }
        employeeIDs.removeAll(removeIDs) ;

        manageAndBoss.remove(1) ;
        //对店长和经理进行交替排班
        boolean isManage = false ;
        for (Employee manageOrBoss : manageAndBoss) {
            for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                if (isManage) break;
                ArrayList<String> locationIDs = employeeNumberDTO.getLocationIDs();
                if(hasPointTimeLocation(locationIDs,"26")){
                    LocalDateTime time = parseLocationIDTOLocalDateTime(locationIDs.get(0));
                    if (time.getDayOfWeek() == DayOfWeek.MONDAY || time.getDayOfWeek() == DayOfWeek.FRIDAY) {
                        isManage = manageLocation(manageOrBoss.getID(), employeeNumberDTO.getLocationIDs(), employeeNumberDTO
                                , systemWorkTimeDTO, systemRestTimeDTO, timesArrayList);
                        }
                    }
                }
            isManage = false ;
        }

        for (String employeeID : employeeIDs) {
            for (PriorityDTO priorityDTO : priorityDTOList) {
                if (employeeID.equals(priorityDTO.getEmployeeID())) {
                    for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                        ArrayList<String> locationIDs = employeeNumberDTO.getLocationIDs();
                        if(hasPointTimeLocation(locationIDs,"26")){
                            LocalDateTime time = parseLocationIDTOLocalDateTime(locationIDs.get(0));
                            if (time.getDayOfWeek() == DayOfWeek.MONDAY || time.getDayOfWeek() == DayOfWeek.FRIDAY) {
                                if (manageLocation(employeeID, employeeNumberDTO.getLocationIDs(), employeeNumberDTO
                                        , systemWorkTimeDTO, systemRestTimeDTO, timesArrayList)) {
                                    changePriority(priorityDTO, 2);
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    /**
     * 获取空闲员工
     */
    private ArrayList<String> getFreeEmployeeIDs(ArrayList<Times> timesArrayList){
        ArrayList<String> employeeIDs = new ArrayList<>();
        for (Times times : timesArrayList) {
            if (times.getPermitTime() - times.getTimeSum() >= MIN_LAST_TIME) {
                employeeIDs.add(times.getEmployeeID());
            }
        }
        return employeeIDs ;
    }

    /**
     * 解析locationID，使其变成日期
     */
    private LocalDateTime parseLocationIDTOLocalDateTime(String locationID){
        return UsualMethodUtils.StringToChineseLocalDateTime(locationID.substring(0,10)) ;
    }


    /**
     * 获取指定时间的locationID
     */
    private boolean hasPointTimeLocation(ArrayList<String> locationIDs ,String pointTime){
        for (String locationID : locationIDs) {
            if (locationID.startsWith(pointTime,11)) {
                return true ;
            }
        }
        return false ;
    }

    /**
     * 获取店长和经理
     */
    private ArrayList<Employee> getManageAndBoss(List<Employee> employees){
        ArrayList<Employee> managers = new ArrayList<>();
        for (Employee employee : employees) {
            if ( employee.getPosition().equals("店长")){
                managers.add(employee) ;
            }
            if (employee.getPosition().equals("经理") ){
                managers.add(employee) ;
                managers.add(employee) ;
            }
        }
        return managers ;
    }

    //TODO 安排在早班或者晚班，要考虑时间问题

    /**
     * 安排还有空余时间的员工
     */
    private void manageFreeEmployee(ArrayList<PriorityDTO> priorityDTOList, ArrayList<EmployeeNumberDTO> employeeNumberDTOList, LocalDateTime localDateTime,
                                    SystemWorkTimeDTO systemWorkTimeDTO,
                                    SystemRestTimeDTO systemRestTimeDTO, ArrayList<Times> timesArrayList) {
        ArrayList<String> employeeIDs = new ArrayList<>();
        for (Times times : timesArrayList) {
            if (times.getPermitTime() - times.getTimeSum() >= MIN_LAST_TIME) {
                employeeIDs.add(times.getEmployeeID());
            }
        }

        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (String employeeID : employeeIDs) {
                if (!employeeID.equals(priorityDTO.getEmployeeID())) continue;
                for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                    //对这个时间段的人是否还可以添加
                    //可以添加，加入进来
                    if (employeeNumberDTO.getNumber() > 0) {
                        ArrayList<String> locationIDs = employeeNumberDTO.getLocationIDs();
                        if (locationIDs.isEmpty()) continue;
                        LocalDateTime time = UsualMethodUtils.StringToChineseLocalDateTime(locationIDs.get(0).substring(0, 10));
                        if (time.getDayOfWeek() != DayOfWeek.SATURDAY && time.getDayOfWeek() != DayOfWeek.SUNDAY) {
                            if (manageLocation(employeeID, employeeNumberDTO.getLocationIDs(), employeeNumberDTO
                                    , systemWorkTimeDTO, systemRestTimeDTO, timesArrayList)) {
                                changePriority(priorityDTO, 2);
                            }
                        }
                    }
                }
            }
        }

        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (String employeeID : employeeIDs) {
                if (!employeeID.equals(priorityDTO.getEmployeeID())) continue;
                for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                    //对这个时间段的人是否还可以添加
                    //可以添加，加入进来
                    ArrayList<String> locationIDs = employeeNumberDTO.getLocationIDs();
                    if (locationIDs.isEmpty()) continue;
                    if (employeeNumberDTO.getNumber() > 0) {
                        manageLocation(employeeID, employeeNumberDTO.getLocationIDs(), employeeNumberDTO
                                , systemWorkTimeDTO, systemRestTimeDTO, timesArrayList);
                    }
                }
            }
        }
    }

    /**
     * 从排班表中移除员工
     */
    private void removeEmployee(ArrayList<PriorityDTO> priorityDTOList, List<Employee> employees) {
        ArrayList<Employee> deleteEmployee = new ArrayList<>();
        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (Employee employee : employees) {
                if (employee.getID().equals(priorityDTO.getEmployeeID())) {
                    if (employee.getPosition().equals("小组长") || employee.getPosition().equals("店长") ||
                            employee.getPosition().equals("经理")) {
                        deleteEmployee.add(employee);
                    }
                }
            }
        }
        employees.removeAll(deleteEmployee);
    }

    /**
     * 对喜好在非工作日工作的员工进行排班
     */
    private void manageWeekDayForEmployee(ArrayList<PriorityDTO> priorityDTOList, ArrayList<EmployeeRoleDTO> employeeRoleDTOList,
                                          ArrayList<EmployeeNumberDTO> employeeNumberDTOList, LocalDateTime localDateTime,
                                          AllRoleDTO allRoleDTO, ArrayList<Times> timesArrayList) {
        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (EmployeeRoleDTO employeeRoleDTO : employeeRoleDTOList) {
                if (employeeRoleDTO.getID().equals(priorityDTO.getEmployeeID())) {
                    manageEmployeeOnWeekDay(priorityDTOList, employeeNumberDTOList, priorityDTO.getEmployeeID()
                            , employeeRoleDTO, localDateTime, allRoleDTO.getSystemWorkTimeDTO()
                            , allRoleDTO.getSystemRestTimeDTO(), timesArrayList);
                    }
                }
            }
        }

    /**
     * 获取全部员工
     */
    private List<Employee> getEmployees(String shopID) {
        return employeeMapper.selectList(new QueryWrapper<Employee>()
                .eq("shop_ID", shopID));
    }

    //-------------------------------------------对剩下的班次进行排班-----------------------------------------------
    public void manageEmployeeForOtherLocation(ArrayList<PriorityDTO> priorityDTOList, ArrayList<EmployeeNumberDTO> employeeNumberDTOList
            , LocalDateTime localDateTime, List<Employee> employees, SystemWorkTimeDTO systemWorkTimeDTO
            , SystemRestTimeDTO systemRestTimeDTO, ArrayList<Times> timesArrayList) {
        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (Employee employee : employees) {
                if (!employee.getID().equals(priorityDTO.getEmployeeID())) continue;
                for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                    if (employeeNumberDTO.getNumber() > 0) {
                        HashMap<String, Integer> type = new HashMap<>();
                        type.put("value", 0);
                        ArrayList<ArrayList<String>> locationIDsOfPointTime
                                = getLocationIDsOfPointTime(localDateTime, employeeNumberDTO, type);
                        if (Objects.isNull(locationIDsOfPointTime)) continue;
                        if (locationIDsOfPointTime.isEmpty()) continue;
                        for (ArrayList<String> locationIDs : locationIDsOfPointTime) {
                            if (manageLocation(priorityDTO.getEmployeeID(), locationIDs
                                    , employeeNumberDTO, systemWorkTimeDTO
                                    , systemRestTimeDTO, timesArrayList)) {
                                switch (type.get("value")) {
                                    //早上
                                    case 1: {
                                        changePriority(priorityDTO, 1);
                                    }

                                    //中午
                                    case 2: {
                                        changePriority(priorityDTO, 1);
                                    }

                                    //下午
                                    case 3: {
                                        changePriority(priorityDTO, 2);
                                    }

                                    //黄金时间段
                                    case 4: {
                                        changePriority(priorityDTO, -2);
                                    }

                                    //晚餐值班
                                    case 5: {
                                        changePriority(priorityDTO, 1);
                                    }

                                    //晚上值4小时班
                                    case 6: {
                                        changePriority(priorityDTO, 2);
                                    }

                                }
                            }
                            //TODO 进行优先级的相加减
                            //TODO 对上面的构件也进行优先级的相加减
                        }
                    }
                }
            }
        }
    }

    /*
     *获取指定类型的时间段的locationID
     */
    private ArrayList<ArrayList<String>> getLocationIDsOfPointTime(LocalDateTime localDateTime
            , EmployeeNumberDTO employeeNumberDTO, HashMap<String, Integer> type) {
        LocalDateTime time = UsualMethodUtils.parseToMonday(localDateTime);
        ArrayList<String> localDateTimeArray = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            String locationPrefix = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            localDateTimeArray.add(locationPrefix);
            time = time.plusDays(1);
        }

        ArrayList<String> checkNumberDTO = new ArrayList<>(employeeNumberDTO.getLocationIDs());
        ArrayList<String> check = new ArrayList<>();
        for (String checkNumber : checkNumberDTO) {
            check.add(checkNumber.substring(11, 13));
        }
        if (!checkNumberDTO.retainAll(new ArrayList<>(Arrays.asList("03", "04", "05", "06")))) {
            type.replace("value", 1);
        } else if (!checkNumberDTO.retainAll(new ArrayList<>(Arrays.asList("07", "08", "09", "10")))) {
            type.replace("value", 2);
        } else if (!checkNumberDTO.retainAll(new ArrayList<>(Arrays.asList("11", "12", "13", "14")))) {
            type.replace("value", 3);
        } else if (!checkNumberDTO.retainAll(new ArrayList<>(Arrays.asList("15", "16", "17", "18")))) {
            type.replace("value", 4);
        } else if (!checkNumberDTO.retainAll(new ArrayList<>(Arrays.asList("19", "20", "21", "22")))) {
            type.replace("value", 5);
        } else if (!checkNumberDTO.retainAll(new ArrayList<>(Arrays.asList("23", "24", "25", "26")))) {
            type.replace("value", 6);
        }

        switch (type.get("value")) {
            //早上
            case 1: {
                String[] IDs = new String[]{"03", "04", "05", "06"};
                return addPointTimeLocationIDs(localDateTimeArray, IDs);
            }

            //中午
            case 2: {
                String[] IDs = new String[]{"07", "08", "09", "10"};
                return addPointTimeLocationIDs(localDateTimeArray, IDs);
            }

            //下午
            case 3: {
                String[] IDs = new String[]{"11", "12", "13", "14"};
                return addPointTimeLocationIDs(localDateTimeArray, IDs);
            }

            //黄金时间段
            case 4: {
                String[] IDs = new String[]{"15", "16", "17", "18"};
                return addPointTimeLocationIDs(localDateTimeArray, IDs);
            }

            //晚餐值班
            case 5: {
                String[] IDs = new String[]{"19", "20", "21", "22"};
                return addPointTimeLocationIDs(localDateTimeArray, IDs);
            }

            //晚上值4小时班
            case 6: {
                String[] IDs = new String[]{"23", "24", "25", "26"};
                return addPointTimeLocationIDs(localDateTimeArray, IDs);
            }

        }
        return null;
    }

    private ArrayList<ArrayList<String>> addPointTimeLocationIDs(ArrayList<String> localDateTimeArray, String... IDs) {
        ArrayList<ArrayList<String>> locationIDs = new ArrayList<>();

        for (String locationPrefix : localDateTimeArray) {
            ArrayList<String> list = new ArrayList<>();
            for (String id : IDs) {
                list.add(locationPrefix + "_" + id);
            }
            locationIDs.add(list);
        }
        return locationIDs;
    }

//-------------------------------------------按照所有员工的意愿进行排班-------------------------------------------

    /**
     * 所有的员工根据他们的员工喜好进行排班
     */
    private void manageAllEmployeesByEmployeeRole(ArrayList<PriorityDTO> priorityDTOList, ArrayList<EmployeeRoleDTO> employeeRoleDTOList
            , ArrayList<EmployeeNumberDTO> employeeNumberDTOList, LocalDateTime localDateTime, SystemWorkTimeDTO systemWorkTimeDTO
            , SystemRestTimeDTO systemRestTimeDTO, ArrayList<Times> timesArrayList) {
        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (EmployeeRoleDTO employeeRoleDTO : employeeRoleDTOList) {
                if (employeeRoleDTO.getID().equals(priorityDTO.getEmployeeID())) {
                    for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                        manageEmployeeByEmployeeRole(employeeRoleDTO, localDateTime, employeeNumberDTO
                                , priorityDTO, systemWorkTimeDTO, systemRestTimeDTO, timesArrayList);
                    }
                }
            }
        }
    }

    /**
     * 对于某一个员工，根据他的喜好进行排班
     */
    private void manageEmployeeByEmployeeRole(EmployeeRoleDTO employeeRoleDTO, LocalDateTime localDateTime,
                                              EmployeeNumberDTO employeeNumberDTO, PriorityDTO priorityDTO, SystemWorkTimeDTO systemWorkTimeDTO
            , SystemRestTimeDTO systemRestTimeDTO, ArrayList<Times> timesArrayList) {
        ArrayList<String> locationIDList = convertEmployeeRole2LocationID(employeeRoleDTO, localDateTime);
        for (String locationID : locationIDList) {
            ArrayList<String> locationIDs = UsualMethodUtils.parseLocation2Locations(locationID);
            ArrayList<String> IDs = employeeNumberDTO.getLocationIDs();
            ArrayList<String> checkLocationIDs = new ArrayList<>(IDs);

            if (!checkLocationIDs.retainAll(locationIDs)) {
                if (priorityDTO.getPriority() > MIN_PRIORITY) {
                    if (!manageLocation(employeeRoleDTO.getID(), locationIDs, employeeNumberDTO
                            , systemWorkTimeDTO, systemRestTimeDTO, timesArrayList)) {
                        //插入一次就对优先级进行一次改变
                        changePriority(priorityDTO, -1);
                    } else {
                        changePriority(priorityDTO, -1);
                    }
                }
            }
        }
    }

//-------------------------------------------黄金时间段的排班---------------------------------------------------

    /**
     * 为黄金时间段安排员工
     */
    private void manageBestTimeLocationForEmployee(ArrayList<PriorityDTO> priorityDTOList
            , ArrayList<EmployeeNumberDTO> employeeNumberDTOList, LocalDateTime localDateTime
            , SystemWorkTimeDTO systemWorkTimeDTO, ArrayList<Times> timesArrayList
            , SystemRestTimeDTO systemRestTimeDTO) {
        ArrayList<ArrayList<String>> locationIDsOfBestTime
                = getLocationIDsOfBestTime(localDateTime);
        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                for (ArrayList<String> bestTime : locationIDsOfBestTime) {
                    ArrayList<String> checkLocationIDs = new ArrayList<>(employeeNumberDTO.getLocationIDs());
                    if (!checkLocationIDs.retainAll(bestTime) && priorityDTO.getPriority() > 6) {
                        if (manageLocation(priorityDTO.getEmployeeID(), employeeNumberDTO.getLocationIDs(),
                                employeeNumberDTO, systemWorkTimeDTO, systemRestTimeDTO, timesArrayList)) {
                            changePriority(priorityDTO, -2);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取黄金时间段的locationID
     */
    private ArrayList<ArrayList<String>> getLocationIDsOfBestTime(LocalDateTime localDateTime) {
        LocalDateTime time = UsualMethodUtils.parseToMonday(localDateTime);
        List<Flow> flowList = flowMapper.selectList(new QueryWrapper<>());
        ArrayList<String> localDateTimeArray = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            String locationPrefix = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            localDateTimeArray.add(locationPrefix);
            time = time.plusDays(1);
        }

        ArrayList<ArrayList<String>> locationIDs = new ArrayList<>();
        ArrayList<String> bestTime = new ArrayList<>();
        bestTime.add("15");
        bestTime.add("16");
        bestTime.add("17");
        bestTime.add("18");
        for (String locationPrefix : localDateTimeArray) {
            ArrayList<String> location = new ArrayList<>();
            for (Flow flow : flowList) {
                if (bestTime.contains(flow.getID())) {
                    location.add(locationPrefix + "_" + flow.getID());
                }
            }
            locationIDs.add(location);
        }
        return locationIDs;
    }

//-------------------------------------------早上和晚上排班-----------------------------------------------------

    /**
     * 安排值早班和晚班的员工
     */
    private void manageMorningAndNightLocationForEmployee(ArrayList<PriorityDTO> priorityDTOList, ArrayList<EmployeeRoleDTO> employeeRoleList,
                                                          ArrayList<EmployeeNumberDTO> employeeNumberDTOList, LocalDateTime localDateTime,
                                                          AllRoleDTO allRoleDTO, ArrayList<Times> timesArrayList) {
        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (EmployeeRoleDTO employeeRoleDTO : employeeRoleList) {
                if (employeeRoleDTO.getID().equals(priorityDTO.getEmployeeID())) {
                    if (!manageMorningAndNight(priorityDTO, employeeNumberDTOList, priorityDTO.getEmployeeID()
                            , employeeRoleDTO, localDateTime, allRoleDTO.getSystemLastTimeDTO(), allRoleDTO.getSystemWorkTimeDTO()
                            , allRoleDTO.getSystemRestTimeDTO(), timesArrayList)) {
                        //没有进行过排班，优先值加2
                        changePriority(priorityDTO, 2);
                    }
                }
            }
        }
    }

    /**
     * 安排值早班和晚班的具体的员工
     */
    private boolean manageMorningAndNight(PriorityDTO priorityDTO, ArrayList<EmployeeNumberDTO> employeeNumberDTOList,
                                          String employeeID, EmployeeRoleDTO employeeRoleDTO, LocalDateTime localDateTime, SystemLastTimeDTO systemLastTimeDTO,
                                          SystemWorkTimeDTO systemWorkTimeDTO, SystemRestTimeDTO systemRestTimeDTO, ArrayList<Times> timesArrayList) {
        ArrayList<String> locationIDs = convertPointTimeEmployeeRole2LocationID(employeeRoleDTO, localDateTime,
                systemLastTimeDTO.getWorkStart() + 2, systemLastTimeDTO.getWorkEnd() - 2);
        int flag = 1;
        int counts = 0 ;
        for (String locationID : locationIDs) {
            ArrayList<String> approprityLocationID = UsualMethodUtils.parseLocation2Locations(locationID);
            for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                ArrayList<String> checkLocationIDs = new ArrayList<>(employeeNumberDTO.getLocationIDs());
                if (!checkLocationIDs.retainAll(approprityLocationID)) {
                    if (counts == 4 ) break;
                    if (manageLocation(employeeID, approprityLocationID, employeeNumberDTO,
                            systemWorkTimeDTO, systemRestTimeDTO, timesArrayList)) {
                            counts ++ ;
                            //成功进行了排班，优先级+1
                            priorityDTO.setPriority(priorityDTO.getPriority() + 1);
                            flag = 0 ;
                        }
                    }
                }
            counts = 0 ;
        }
        //是否成功进行了排班
        return flag == 0;
    }


    /**
     * TODO 修改,将用户偏好在早班和晚班的员工转换成LocationID
     */
    public ArrayList<String> convertPointTimeEmployeeRole2LocationID(EmployeeRoleDTO employeeRoleDTO
            , LocalDateTime localDateTime, double startTime, double endTime) {
        ArrayList<String> locationIDs = new ArrayList<>();
        LocalDateTime times = UsualMethodUtils.parseToMonday(localDateTime);
        //获取员工偏好工作日地具体日期
        ArrayList<Integer> employeeWorkDayList = employeeRoleDTO.getEmployeeWorkDayDTO().getEmployeeWorkDayList();
        //获取前缀
        ArrayList<String> locationPrefixs = new ArrayList<>();
        for (Integer workDay : employeeWorkDayList) {
            LocalDateTime time = times;
            for (int i = 1; i < workDay; i++) {
                time = time.plusDays(1);
            }
            locationPrefixs.add(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_");
        }

        ArrayList<String> locationRealIDs = new ArrayList<>();
        ArrayList<String> employeeWorkTimeList = employeeRoleDTO.getEmployeeWorkTimeDTO().getEmployeeWorkTimeList();
        for (String employeeWorkTime : employeeWorkTimeList) {
            String[] workTimes = employeeWorkTime.split("~");
            for (String workTime : workTimes) {
                Integer hour = parseTime(workTime, 1);
                //当时间超过开店时间的两小时时，就可以视为值早班；当时间在关店时间的前两个小时时，就可以视为晚班
                if (hour > (startTime) && hour < (endTime)) continue;
                Integer minu = parseTime(workTime, 2);
                locationRealIDs.add(parseTime2LocationRealID(hour, minu, parseTime(workTimes[1], 1)));
            }
        }

        for (String locationPrefix : locationPrefixs) {
            if (locationRealIDs.isEmpty()) continue;
            for (String locationRealID : locationRealIDs) {
                locationIDs.add(locationPrefix + locationRealID);
            }
        }

        return locationIDs;
    }

//------------------------------------------在午餐和晚餐时间段进行排班---------------------------------------------

    /**
     * 对喜好在午餐和晚餐时间段值班的员工进行排班
     */
    private void manageLunchAndDinnerLocationForEmployee(ArrayList<PriorityDTO> priorityDTOList, ArrayList<EmployeeRoleDTO> employeeRoleList,
                                                         ArrayList<EmployeeNumberDTO> employeeNumberDTOList
            , LocalDateTime localDateTime, AllRoleDTO allRoleDTO, ArrayList<Times> timesArrayList) {
        for (PriorityDTO priorityDTO : priorityDTOList) {
            for (EmployeeRoleDTO employeeRoleDTO : employeeRoleList) {
                if (employeeRoleDTO.getID().equals(priorityDTO.getEmployeeID())) {
                    if (!manageLunchAndDinner(priorityDTO, employeeNumberDTOList, priorityDTO.getEmployeeID()
                            , employeeRoleDTO, localDateTime, allRoleDTO.getSystemWorkTimeDTO(), timesArrayList
                            , allRoleDTO.getSystemRestTimeDTO())) {
                        //没有进行过排班，优先值加2
                        changePriority(priorityDTO, 2);
                    }
                }
            }
        }
    }

    /**
     * 对喜好在午餐和晚餐时间段值班的具体的员工进行排班
     */
    private boolean manageLunchAndDinner(PriorityDTO priorityDTO, ArrayList<EmployeeNumberDTO> employeeNumberDTOList,
                                         String employeeID, EmployeeRoleDTO employeeRoleDTO, LocalDateTime localDateTime
            , SystemWorkTimeDTO systemWorkTimeDTO, ArrayList<Times> timesArrayList
            , SystemRestTimeDTO systemRestTimeDTO) {
        ArrayList<String> locationIDs = convertPointTimeEmployeeRole2LocationID(employeeRoleDTO, localDateTime,
                systemRestTimeDTO.getLunchTimeStart(), systemRestTimeDTO.getLunchTimeEnd(),
                systemRestTimeDTO.getDinnerTimeStart(), systemRestTimeDTO.getDinnerTimeEnd());
        int flag = 1;
        int anotherFlag = 1;
        for (String locationID : locationIDs) {
            ArrayList<String> approprityLocationID = UsualMethodUtils.parseLocation2Locations(locationID);
            for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                ArrayList<String> checkLocations = employeeNumberDTO.getLocationIDs();
                if (checkLocations.retainAll(approprityLocationID)) {
                    //TODO 是否成功进行了排班的判断条件是否应该修改一下
                    if (manageLocation(employeeID, approprityLocationID, employeeNumberDTO
                            , systemWorkTimeDTO, systemRestTimeDTO, timesArrayList)) {
                        anotherFlag--;
                        if (anotherFlag == 0) {
                            //进行了排班，优先值加1
                            priorityDTO.setPriority(priorityDTO.getPriority() + 3);
                            flag = 0;
                        }
                    }
                }
            }
        }
        //是否成功进行了排班
        return flag == 0;
    }

    /**
     * 对员工喜好的的时间段，恰好处于指定时间早餐和晚餐时间段的员工进行排班
     */
    private ArrayList<String> convertPointTimeEmployeeRole2LocationID(EmployeeRoleDTO employeeRoleDTO, LocalDateTime localDateTime,
                                                                      double lunchTimeStart, double lunchTimeEnd, double dinnerTimeStart, double dinnerTimeEnd) {
        ArrayList<String> locationIDs = new ArrayList<>();
        LocalDateTime times = UsualMethodUtils.parseToMonday(localDateTime);
        ArrayList<Integer> employeeWorkDayList = employeeRoleDTO.getEmployeeWorkDayDTO().getEmployeeWorkDayList();
        ArrayList<String> locationPrefixs = new ArrayList<>();
        for (Integer workDay : employeeWorkDayList) {
            LocalDateTime time = times;
            for (int i = 1; i < workDay; i++) {
                time = time.plusDays(1);
            }
            locationPrefixs.add(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_");
        }

        ArrayList<String> locationRealIDs = new ArrayList<>();
        ArrayList<String> employeeWorkTimeList = employeeRoleDTO.getEmployeeWorkTimeDTO().getEmployeeWorkTimeList();
        for (String employeeWorkTime : employeeWorkTimeList) {
            String[] workTimes = employeeWorkTime.split("~");
            if (checkLunchTimeAndDinnerTime(workTimes[0], workTimes[1], lunchTimeStart
                    , lunchTimeEnd, dinnerTimeStart, dinnerTimeEnd)) {
                //合适,直接进行安排
                Integer startHour = parseTime(workTimes[0], 1);
                Integer endHour = parseTime(workTimes[1], 1);
                Integer minu = parseTime(workTimes[0], 0) > parseTime(workTimes[1], 0)
                        ? parseTime(workTimes[0], 0) : parseTime(workTimes[1], 0);
                while (startHour <= endHour) {
                    //TODO 这里有点问题，有时候会出现三次值班的情况
                    locationRealIDs.add(parseTime2LocationRealID(startHour, minu, parseTime(workTimes[1], 1)));
                    startHour++;
                }
            }
        }

        for (String locationPrefix : locationPrefixs) {
            for (String locationRealID : locationRealIDs) {
                locationIDs.add(locationPrefix + locationRealID);
            }
        }

        return locationIDs;
    }

    /**
     *TODO 拥有完全覆盖午餐和晚餐时间段的员工时，需要额外的一名员工存在.
     */

    /**
     * 检查员工喜欢的时间段是否完全覆盖早餐和晚餐的时间段
     */
    private boolean checkLunchTimeAndDinnerTime(String workTimeStart, String workTimeEnd, double lunchTimeStart,
                                                double lunchTimeEnd, double dinnerTimeStart, double dinnerTimeEnd) {
        Integer workStartHour = parseTime(workTimeStart, 1);
        Integer workEndHour = parseTime(workTimeEnd, 1);
        return workStartHour <= lunchTimeStart && workEndHour - workStartHour >= lunchTimeEnd - lunchTimeStart
                || workStartHour <= dinnerTimeStart && workEndHour - workStartHour >= dinnerTimeEnd - dinnerTimeStart;
    }

    /**
     * 对时间信息进行解析
     */
    private Integer parseTime(String time, Integer isHour) {
        if (time.length() != 5 && !time.substring(0, 2).startsWith("0"))
            time = "0" + time;
        if (isHour == 1) return Integer.parseInt(time.substring(0, 2));
        return Integer.parseInt(time.substring(3, 5));
    }

    /**
     * 将时间信息转换成班次具体ID
     */
    private String parseTime2LocationRealID(Integer hour, Integer min, Integer endTime) {
        Integer temp_hour = (hour - FLOW_START) * 2;
        Integer temp_min = (min / 30 + 1);
        if (hour.equals(endTime)) {
            return (temp_hour + temp_min - 1) < 10 ? "0" + (temp_hour + temp_min - 1) : (temp_hour + temp_min - 1) + "";
        } else {
            return (temp_hour + temp_min) < 10 ? "0" + (temp_hour + temp_min) : (temp_hour + temp_min) + "";
        }
    }

//-----------------------------------------对小组和小组成员进行排班-----------------------------------------------

    /**
     * 安排其他小组进行排班
     */
    private void manageOtherGroup(ArrayList<PriorityDTO> priorityDTOList, List<Employee> employees,
                                  ArrayList<EmployeeRoleDTO> employeeRoleDTOList, AllRoleDTO allRoleDTO,
                                  ArrayList<EmployeeNumberDTO> employeeNumberDTOList, ArrayList<Times> timesArrayList) {
        for (PriorityDTO priorityDTO : priorityDTOList) {
            //获取成员信息
            Employee group = new Employee();
            for (Employee employee : employees) {
                if (employee.getID().equals(priorityDTO.getEmployeeID()) && employee.getPosition().equals("小组长")) {
                    group = employee;
                    break;
                }
            }
            if (group.getID() == null) continue;
            for (int i = 0; i < 3; i++) {
                for (EmployeeRoleDTO employeeRoleDTO : employeeRoleDTOList) {
                    if (employeeRoleDTO.getID().equals(group.getID())) {
                        if (manageGroupLocations(getAppropriateEmployeeNumber(allRoleDTO, employeeNumberDTOList), priorityDTO, allRoleDTO.getSystemWorkTimeDTO()
                                , allRoleDTO.getSystemRestTimeDTO(), allRoleDTO.getSystemGroupPositionDTO(), timesArrayList)) {
                            changePriority(priorityDTO, 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * 对其他小组的人进行排班
     */
    public boolean manageGroupLocations(ArrayList<EmployeeNumberDTO> employeeNumberDTOList, PriorityDTO priorityDTO,
                                        SystemWorkTimeDTO systemWorkTimeDTO, SystemRestTimeDTO systemRestTimeDTO
            , SystemGroupPositionDTO systemGroupPositionDTO, ArrayList<Times> timesArrayList) {
        //根据小组长的喜好进行排班
        //根据优先级进行排班，priorityDTO里含有顺序信息
        int flag = 1;
        int counts = 0 ;
        //获取员工的偏好信息
        for (EmployeeNumberDTO employeeNumber : employeeNumberDTOList) {
            if (counts==4) break;
            ArrayList<String> locationIDs = employeeNumber.getLocationIDs();
            if (!locationIDs.isEmpty()) {
                if (employeeNumber.getNumber() != 0) {
                    //获取小组的成员
                    List<Employee> groupEmployees
                            = getGroupEmployees(priorityDTO.getEmployeeID());
                    //获取小组中与系统要求的人员一致的成员的成员
                    List<Employee> groupEmployeeApporiteSystemRole
                            = getGroupEmployeeApporiteSystemRole(groupEmployees, systemGroupPositionDTO);
                    if (manageLocation(priorityDTO.getEmployeeID(), locationIDs
                            , employeeNumber, systemWorkTimeDTO, systemRestTimeDTO, timesArrayList)) {
                        changePriority(priorityDTO, -1);
                        flag = 0;
                        counts ++ ;
                    } else {
                        continue;
                    }
                    if (groupEmployeeApporiteSystemRole.isEmpty()) return false;
                    for (Employee groupEmployee : groupEmployeeApporiteSystemRole) {
                        manageLocation(groupEmployee.getID(), locationIDs, employeeNumber,
                                systemWorkTimeDTO, systemRestTimeDTO, timesArrayList);
                    }
                }
            }
        }

        return flag != 1;
    }

    /**
     * 按照小组进行排班
     */
    public void manageGroup(ArrayList<PriorityDTO> priorityDTOList, List<Employee> employees,
                            ArrayList<EmployeeRoleDTO> employeeRoleDTOList, AllRoleDTO allRoleDTO,
                            ArrayList<EmployeeNumberDTO> employeeNumberDTOList, LocalDateTime localDateTime, ArrayList<Times> timesArrayList) {
        for (PriorityDTO priorityDTO : priorityDTOList) {
            //获取成员信息
            Employee group = new Employee();
            for (Employee employee : employees) {
                if (employee.getID().equals(priorityDTO.getEmployeeID()) && employee.getPosition().equals("小组长")) {
                    group = employee;
                    break;
                }
            }
            if (group.getID() == null) continue;
            for (EmployeeRoleDTO employeeRoleDTO : employeeRoleDTOList) {
                if (employeeRoleDTO.getID().equals(group.getID())) {
                    if (!manageGroupLocations(employeeRoleDTO, allRoleDTO.getSystemGroupPositionDTO()
                            , getAppropriateEmployeeNumber(allRoleDTO, employeeNumberDTOList)
                            , localDateTime, priorityDTO, allRoleDTO.getSystemWorkTimeDTO()
                            , allRoleDTO.getSystemRestTimeDTO(), timesArrayList ,allRoleDTO)) {
                        changePriority(priorityDTO, 1);
                    }
                }
            }
        }

        ArrayList<Employee> manages = getManageAndBoss(employees);

        Random random = new Random();
        random.setSeed(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        int ran = random.nextInt()%2;
        //对店长和经理进行交替排班
        for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
            int index = (ran)%2 ;
            Employee manage = manages.get(index);
            DayOfWeek dayOfWeek = UsualMethodUtils.StringToChineseLocalDateTime(employeeNumberDTO.getLocationIDs().get(0).substring(0, 10)).getDayOfWeek();
            if (dayOfWeek==DayOfWeek.SATURDAY || dayOfWeek==DayOfWeek.SUNDAY) continue;
            if (manage.getPosition().equals("经理") || manage.getPosition().equals("店长")) {
                ran++ ;
                ArrayList<String> locationIDs = employeeNumberDTO.getLocationIDs();
                if (employeeNumberDTO.getNumber() > allRoleDTO.getGroupRoleDTO().getDeadLineEmployee()) {
                    //获取小组的成员
                    ArrayList<Employee> groupEmployees = new ArrayList<>(employees);
                    groupEmployees.removeIf(groupEmployee -> groupEmployee.getPosition().equals("店长") || groupEmployee.getPosition().equals("经理"));
                    //获取小组中与系统要求的人员一致的成员的成员
                    List<Employee> groupEmployeeApporiteSystemRole
                            = getGroupEmployeeApporiteSystemRole(groupEmployees, allRoleDTO.getSystemGroupPositionDTO());
                    if (manageLocation(manage.getID(), locationIDs, employeeNumberDTO, allRoleDTO.getSystemWorkTimeDTO()
                            , allRoleDTO.getSystemRestTimeDTO(), timesArrayList)) {
                        if (groupEmployeeApporiteSystemRole.isEmpty()) return ;
                        for (Employee groupEmployee : groupEmployeeApporiteSystemRole) {
                            manageLocation(groupEmployee.getID(), locationIDs, employeeNumberDTO,
                                    allRoleDTO.getSystemWorkTimeDTO(), allRoleDTO.getSystemRestTimeDTO(), timesArrayList);
                        }
                    }
                }
            }
        }
    }

    /**
     * 每个班次对应的locationID号里需要的员工数量
     */
    public ArrayList<EmployeeNumberDTO> getPeopleNumInEveryLocation( FlowRoleDTO flowRoleDTO , LocalDateTime localDateTime ,AllRoleDTO allRoleDTO){
        //创建班次的人数集合
        ArrayList<EmployeeNumberDTO> employeeNumberDTOList = new ArrayList<>() ;
        //获取本周的星期一
        LocalDateTime time = UsualMethodUtils.parseToMonday(localDateTime);
        //TODO 更改逻辑，将其变成一个处于浮动区间的数值 获取人流量基数
        double baseNum = flowRoleDTO.getBaseNum();
        List<Flow> flowList = flowMapper.selectList(new QueryWrapper<>());
        ArrayList<String> localDateTimeArray = new ArrayList<>();
        //存储本周所有的时间
        for(int i = 1 ; i <= 7 ; i++ ){
            String locationPrefix = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            localDateTimeArray.add(locationPrefix) ;
            time = time.plusDays(1) ;
        }

        int count = 1 ;
        int con = 1 ;
        ArrayList<String> locationIDs = new ArrayList<>();
        double flowSum = 0 ;
        flowList.remove(0) ;
        flowList.remove(0);
        for (String locationPrefix : localDateTimeArray) {
            for (Flow flow : flowList) {
                if (count<4 && con!=24){
                    String locationID = locationPrefix + "_" + flow.getID();
                    locationIDs.add(locationID) ;
                    flowSum += flow.getPermitFlow() ;
                    count++ ;
                    con++ ;
                }
                else if (count == 4 || con==24){
                    flowSum += flow.getPermitFlow() ;
                    //获取真正的流量总数
                    flowSum = flowSum/4 ;
                    String locationID = locationPrefix + "_" + flow.getID();
                    locationIDs.add(locationID) ;
                    EmployeeNumberDTO employeeNumberDTO = new EmployeeNumberDTO();
                    employeeNumberDTO.setNumber(getRealNum(flowSum, baseNum));
                    employeeNumberDTO.setLocationIDs(locationIDs);
                    employeeNumberDTOList.add(employeeNumberDTO);
                    locationIDs = new ArrayList<>() ;
                    flowSum = 0 ;
                    count = 1 ;
                    if (con==26) {
                        con=1 ;
                        continue;
                    }
                    con++ ;
                }

            }
        }

        CloseRoleDTO closeRoleDTO = allRoleDTO.getCloseRoleDTO();
        StockRoleDTO stockRoleDTO = allRoleDTO.getStockRoleDTO();
        OpenRoleDTO openRoleDTO = allRoleDTO.getOpenRoleDTO();

        for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
            if (time.getDayOfWeek() == DayOfWeek.MONDAY || time.getDayOfWeek() == DayOfWeek.FRIDAY) {
                if (hasPointTimeLocation(employeeNumberDTO.getLocationIDs() , "26")) {
                    int requirePeopleNumber = (int) closeRoleDTO.getFomula() + stockRoleDTO.getMinEmployee() ;
                    employeeNumberDTO.setNumber(requirePeopleNumber);
                }
            } else {
                if (hasPointTimeLocation(employeeNumberDTO.getLocationIDs() , "26")) {
                    int requirePeopleNumber = (int) closeRoleDTO.getFomula() ;
                    employeeNumberDTO.setNumber(requirePeopleNumber);
                }
            }
        }

        for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
            if (time.getDayOfWeek() == DayOfWeek.MONDAY || time.getDayOfWeek() == DayOfWeek.FRIDAY) {
                if (hasPointTimeLocation(employeeNumberDTO.getLocationIDs() , "03")) {
                    int requirePeopleNumber = (int)openRoleDTO.getFomula() ;
                    employeeNumberDTO.setNumber(requirePeopleNumber);
                }
            }
        }

        return employeeNumberDTOList;
    }

    /**
    * 获取所有的locationID
     */
    public ArrayList<String> getEveryLocationID(LocalDateTime localDateTime){
        //获取本周的星期一
        LocalDateTime time = UsualMethodUtils.parseToMonday(localDateTime);
        //TODO 更改逻辑，将其变成一个处于浮动区间的数值 获取人流量基数
        List<Flow> flowList = flowMapper.selectList(new QueryWrapper<>());
        ArrayList<String> localDateTimeArray = new ArrayList<>();
        //存储本周所有的时间
        for(int i = 1 ; i <= 7 ; i++ ){
            String locationPrefix = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            localDateTimeArray.add(locationPrefix) ;
            time = time.plusDays(1) ;
        }

        int count = 1 ;
        int con = 1 ;
        ArrayList<String> locationIDs = new ArrayList<>();
        for (String locationPrefix : localDateTimeArray) {
            for (Flow flow : flowList) {
                if (count<4 && con!=24){
                    String locationID = locationPrefix + "_" + flow.getID();
                    locationIDs.add(locationID) ;
                    count++ ;
                    con++ ;
                }
                else if (count == 4 || con==24){
                    String locationID = locationPrefix + "_" + flow.getID();
                    locationIDs.add(locationID) ;
                    count = 1 ;
                    if (con==24) {
                        con=1 ;
                        continue;
                    }
                    con++ ;
                }
            }
        }

        return locationIDs;
    }

    /**
     * 获取符合条件的小组长
     */
    public ArrayList<EmployeeNumberDTO> getAppropriateEmployeeNumber (AllRoleDTO allRoleDTO
            , ArrayList<EmployeeNumberDTO> EmployeeNumList) {
        ArrayList<EmployeeNumberDTO> appropriateEmployeeNumberDTO = new ArrayList<>();
        FlowRoleDTO flowRoleDTO = allRoleDTO.getFlowRoleDTO();
        //当人数达到人数要求的时候就要对小组长进行排班
        for (EmployeeNumberDTO employeeNumberDTO : EmployeeNumList) {
            //获取大于BaseNum时进行排班
            if (employeeNumberDTO.getNumber()>flowRoleDTO.getBaseNum()){
                appropriateEmployeeNumberDTO.add(employeeNumberDTO) ;
            }
        }
        return appropriateEmployeeNumberDTO;
    }

    /**
     * 安排小组的班次信息
     */
    public boolean manageGroupLocations(EmployeeRoleDTO employeeRoleDTO , SystemGroupPositionDTO systemGroupPositionDTO
            , ArrayList<EmployeeNumberDTO> employeeNumberDTOList, LocalDateTime localDateTime, PriorityDTO priorityDTO ,
            SystemWorkTimeDTO systemWorkTimeDTO ,SystemRestTimeDTO systemRestTimeDTO,ArrayList<Times> timesArrayList
            , AllRoleDTO allRoleDTO){
        //根据小组长的喜好进行排班
        //根据优先级进行排班，priorityDTO里含有顺序信息
        int flag = 1 ;
            //获取员工的偏好信息

        if (priorityDTO.getEmployeeID().equals(employeeRoleDTO.getID())){
            //根据员工偏好信息获取员工喜欢的时间段
            ArrayList<String> employeeRoleLocationIDs
                    = convertEmployeeRole2LocationID(employeeRoleDTO, localDateTime);
            for (EmployeeNumberDTO employeeNumber : employeeNumberDTOList){
                ArrayList<String> locationIDs = employeeNumber.getLocationIDs();
                for (String employeeRoleLocationID : employeeRoleLocationIDs) {
                    //对员工喜欢的时间段进行扩充
                    ArrayList<String> locations = UsualMethodUtils.parseLocation2Locations(employeeRoleLocationID);
                    ArrayList<String> checkLocations = new ArrayList<>(locationIDs);
                    if (!checkLocations.retainAll(locations)&&!locations.isEmpty()&&!locationIDs.isEmpty()){
                        if (employeeNumber.getNumber()>=allRoleDTO.getGroupRoleDTO().getDeadLineEmployee()) {
                            flag = 0 ;
                            //获取小组的成员
                            List<Employee> groupEmployees
                                    = getGroupEmployees(priorityDTO.getEmployeeID());
                            //获取小组中与系统要求的人员一致的成员的成员
                            List<Employee> groupEmployeeApporiteSystemRole
                                    = getGroupEmployeeApporiteSystemRole(groupEmployees, systemGroupPositionDTO);
                            if(manageLocation(priorityDTO.getEmployeeID(),locationIDs
                                    ,employeeNumber,systemWorkTimeDTO ,systemRestTimeDTO,timesArrayList)){
                                changePriority(priorityDTO,-1);
                            } else {
                                continue;
                            }
                            if (groupEmployeeApporiteSystemRole.isEmpty()) return false ;
                            for (Employee groupEmployee : groupEmployeeApporiteSystemRole) {
                                manageLocation(groupEmployee.getID(), locationIDs, employeeNumber ,
                                        systemWorkTimeDTO ,systemRestTimeDTO,timesArrayList);
                            }
                        }
                    }
                }
            }
        }

        return flag != 1 ;
    }

    /**
     *获取小组员工信息
     */
    public List<Employee> getGroupEmployees(String groupID){
        return employeeMapper.selectList(new QueryWrapper<Employee>().eq("belong", groupID));
    }

    /**
     * 获取符合条件的组内员工
     */
    public List<Employee> getGroupEmployeeApporiteSystemRole(List<Employee> groupEmployees
            ,SystemGroupPositionDTO systemGroupPositionDTO ){
        HashMap<String, Integer> positionMap = systemGroupPositionDTO.getPositionMap();
        ArrayList<Employee> apporityEmployees = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : positionMap.entrySet()) {
            Integer number = entry.getValue();
            String position = entry.getKey();
            for(int i = 1 ; i <= number ; i++) {
                for (Employee groupEmployee : groupEmployees) {
                    if (groupEmployee.getPosition().equals(position)){
                        apporityEmployees.add(groupEmployee) ;
                    }
                }
            }
        }
        return apporityEmployees ;
    }


//-----------------------------------------工具类-------------------------------------------------------------

    /**
     * 删除所有的记录
     */
    private void deleteAllLocations(LocalDateTime localDateTime, List<Employee> employeeArrayList){
        ArrayList<String> locationIDs = getEveryLocationID(localDateTime);
        locationMapper.deleteBatchIds(locationIDs) ;
        for (String locationID : locationIDs) {
            workingMapper.delete(new QueryWrapper<Working>().eq("location_ID",locationID)) ;
        }
        for (Employee employee : employeeArrayList) {
            Times times = new Times();
            times.setEmployeeID(employee.getID());
            times.setTimeSum(0);
            times.setPermitTime(40);
            times.setCounts(0);
            timesMapper.update(times,new QueryWrapper<Times>().eq("employee_ID",times.getEmployeeID())) ;
        }
    }

    /**
     * 获取数据库中的priority值
     */
    public void getPirority(ArrayList<PriorityDTO> priorityList) {
        String IDs = "";
        for (PriorityDTO priorityDTO : priorityList) {
            IDs += priorityDTO.getEmployeeID() + "," ;
        }
        IDs = IDs.substring(0,IDs.length()-1) ;
        List<Priority> priorities = priorityMapper.selectList(new QueryWrapper<Priority>().inSql("employee_ID",IDs)) ;
        for (Priority priority : priorities) {
            for (PriorityDTO priorityDTO : priorityList) {
                priorityDTO.setPriority(priority.getPriority());
            }
        }
    }

    /**
     * 获取每个班次需要排班的员工数
     */
    public int getRealNum(double permitFlow , double baseNum){
        return (int)
                (permitFlow/baseNum > (int)permitFlow/baseNum
                        ? (int)permitFlow/baseNum + 1
                        : (int)permitFlow/baseNum);
    }

    /**
     * 初始化每个员工的优先值
     */
    public ArrayList<PriorityDTO> getInitOneShopPriorityDTOs(String shopID){
        ArrayList<PriorityDTO> priorityDTOList = new ArrayList<>();
        List<Employee> employeeLsit = employeeMapper.selectList(new QueryWrapper<Employee>()
                .eq("shop_ID", shopID).ne("position","经理").ne("position","店长"));
        for (Employee employee : employeeLsit) {
            PriorityDTO priorityDTO = new PriorityDTO();
            priorityDTO.setEmployeeID(employee.getID());
            priorityDTO.setPriority(0);
            priorityDTOList.add(priorityDTO) ;
        }
        return priorityDTOList ;
    }

    /**
     * 获取所有员工的偏好信息
     */
    public  ArrayList<EmployeeRoleDTO> getEmployeeRoleArrayList(String shopID){
        ArrayList<EmployeeRoleDTO>  employeeRoleDTOList = new ArrayList<>();
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);
        List<Employee> employeeList = employeeMapper.selectList(new QueryWrapper<Employee>().eq("shop_ID", shopID)
                .ne("position","经理").ne("position","店长"));
        for (Employee employee : employeeList) {
            List<EmployeeRole> employeeRoles = employeeRoleMapper.selectList(new QueryWrapper<EmployeeRole>()
                    .eq("employee_ID", employee.getID()));
            EmployeeRoleDTO employeeRoleDTO = new EmployeeRoleDTO();
            employeeRoleDTO.setID(employee.getID());
            //先对普通店员按照ID号进行优先排班，剩下没有排到自己想要的班次的人，优先级加一
            for (EmployeeRole employeeRole : employeeRoles) {
                switch(employeeRole.getHobbyType()) {
                    case EMPLOYEEROLE_TYPE1 :{
                        employeeRoleDTO.setEmployeeWorkDayDTO
                                (roleUtils.parseEmployeeRole(EmployeeWorkDayDTO.class ,EMPLOYEEROLE_TYPE1 , employee.getID()));
                    }

                    case EMPLOYEEROLE_TYPE2 :{
                        employeeRoleDTO.setEmployeeWorkTimeDTO
                                (roleUtils.parseEmployeeRole(EmployeeWorkTimeDTO.class ,EMPLOYEEROLE_TYPE2 , employee.getID()));
                    }

                    case EMPLOYEEROLE_TYPE3 :{
                        employeeRoleDTO.setEmployeeLastTimeDTO
                                (roleUtils.parseEmployeeRole(EmployeeLastTimeDTO.class ,EMPLOYEEROLE_TYPE3 , employee.getID()));
                    }
                }
            }
            employeeRoleDTOList.add(employeeRoleDTO) ;
        }
        return employeeRoleDTOList ;
    }

    /**
     * 将员工的喜好转化成固定的班次LocationID
     */
    public ArrayList<String> convertEmployeeRole2LocationID(EmployeeRoleDTO employeeRoleDTO, LocalDateTime localDateTime){
        ArrayList<String> locationIDs = new ArrayList<>();
        LocalDateTime times = UsualMethodUtils.parseToMonday(localDateTime);
        //获取员工偏好的工作日期
        ArrayList<Integer> employeeWorkDayList = employeeRoleDTO.getEmployeeWorkDayDTO().getEmployeeWorkDayList();
        ArrayList<String> locationPrefixs = new ArrayList<>();
        for (Integer workDay : employeeWorkDayList) {
            LocalDateTime time = times ;
            for(int i = 1 ; i < workDay ; i++){
                time = time.plusDays(1) ;
            }
            locationPrefixs.add(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_") ;
        }

        ArrayList<String> locationRealIDs = new ArrayList<>();
        ArrayList<String> employeeWorkTimeList = employeeRoleDTO.getEmployeeWorkTimeDTO().getEmployeeWorkTimeList();
        //将用户偏好的工作日期转换成工作班次ID
        for (String employeeWorkTime : employeeWorkTimeList) {
            String[] workTimes = employeeWorkTime.split("~");
            for (String workTime : workTimes) {
                Integer hour = parseTime(workTime,1) ;
                Integer min = parseTime(workTime,0) ;
                locationRealIDs.add(parseTime2LocationRealID(hour,min,parseTime(workTimes[1],1))) ;
            }
        }

        for (String locationPrefix : locationPrefixs) {
            for (String locationRealID : locationRealIDs) {
                locationIDs.add(locationPrefix + locationRealID) ;
            }
        }

        return locationIDs;
    }

    /**
     * 获取系统规则中要求的最少人数
     */
    public Integer getSystemGroupNumer(SystemGroupPositionDTO systemGroupPositionDTO){
        HashMap<String, Integer> positionMap = systemGroupPositionDTO.getPositionMap();
        int sum = 0;
        for (Map.Entry<String, Integer> entry : positionMap.entrySet()) {
            sum += entry.getValue() ;
        }
        return sum ;
    }

    /**
     *排班
     */
    public boolean manageLocation(String employeeID , ArrayList<String> locationIDList
            ,EmployeeNumberDTO employeeNumberDTO ,SystemWorkTimeDTO systemWorkTimeDTO
            ,SystemRestTimeDTO systemRestTimeDTO ,ArrayList<Times> timesArrayList)
    {
        if (employeeNumberDTO.getNumber() == 0) return false;
        ArrayList<Working> workingList = new ArrayList<>();
        //检查班次是否合规
        if(!locationIsRegularity( systemWorkTimeDTO , employeeID, systemRestTimeDTO , locationIDList ,timesArrayList)){
            return false ;
        }
        Times employeeTimes = null;
        for (Times time : timesArrayList) {
            if (time.getEmployeeID().equals(employeeID)){
                employeeTimes = time ;
            }
        }
        if (Objects.isNull(employeeTimes)) return false ;

        /*
         * 早晚班额外增加时长
         */
        if (isMorning(locationIDList)){
            if(employeeTimes.getPermitTime() - employeeTimes.getTimeSum() - 2 - CLEANTIME_BEFOREOPEN < 0) {
                return false ;
            }
        } else if (isNight(locationIDList)) {
            if(employeeTimes.getPermitTime() - employeeTimes.getTimeSum() - 2 - CLEANTIME_BEFORECLOSE < 0) {
                return false ;
            }
        }
        employeeNumberDTO.setNumber(employeeNumberDTO.getNumber() - 1);

        //查看员工本天的值班次数是否超过了规定水准，以及是否存在值班超时的情况
        for (String locationID : locationIDList) {
            //根据员工ID号进行查询，将得到的内容填充到working中
            Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", employeeID));
            Working working = new Working();
            working.setName(employee.getName());
            working.setPosition(employee.getPosition());
            working.setLocationID(locationID);
            working.setEmployeeID(employeeID);
            working.setShopID(employee.getShopID());
            //查看同一个人是否被填入了相同的位置
            Working work = workingMapper.selectOne(new QueryWrapper<Working>()
                    .eq("employee_ID", employeeID)
                    .eq("location_ID", locationID));
            if (!Objects.isNull(work))
                return false;

            //根据用户选中的班次ID进行查询
            Location location = locationMapper.selectOne(new QueryWrapper<Location>().eq("ID", locationID));
            Location newLocation = new Location();
            changeEmployeeTimes(employeeTimes,MIN_WORKINGTIME,1);

            //如果查询结果为空，说明该班次没有人，则将current_num赋值为1；否则，在current_num的基础上加1
            if (Objects.isNull(location)) {
                newLocation.setID(locationID);
                newLocation.setCurrentNumber(1);
                String flowID = UsualMethodUtils.getRealFlowID(locationID);
                newLocation.setFlowID(flowID);
                locationMapper.insert(newLocation) ;
            }else{
                newLocation.setID(locationID);
                newLocation.setCurrentNumber(location.getCurrentNumber()+1);
                newLocation.setFlowID(UsualMethodUtils.getRealFlowID(locationID));
                locationMapper.update(newLocation,new UpdateWrapper<Location>().eq("ID",locationID)) ;
            }

            //存入
            workingList.add(working) ;
        }
        for (Working working : workingList) {
            workingMapper.insert(working) ;
        }

        /*
         * 早晚班额外增加时长
         */
        if (isMorning(locationIDList)){
            changeEmployeeTimes(employeeTimes,CLEANTIME_BEFOREOPEN,0);
        } else if (isNight(locationIDList)) {
            changeEmployeeTimes(employeeTimes,CLEANTIME_BEFORECLOSE,0);
        }
        employeeNumberDTO.setNumber(employeeNumberDTO.getNumber() - 1);
        return true ;
    }

    /**
     * 检测是否值早班
     */
    private boolean isMorning(ArrayList<String> locationIDs){
        boolean isMorn = false ;
        for (String locationID : locationIDs) {
            if (locationID.startsWith("03", 11)) {
                isMorn = true ;
                break;
            }
        }
        return isMorn ;
    }

    /**
     * 检测是否值晚班
     */
    private boolean isNight(ArrayList<String> locationIDs){
        boolean isNight = false ;
        for (String locationID : locationIDs) {
            if (locationID.startsWith("26", 11)) {
                isNight = true ;
                break;
            }
        }
        return isNight ;
    }

    /**
     * 获取员工总时长
     */
    private ArrayList<Times> getEmployeeTimes(){
        return new ArrayList<>(timesMapper.selectList(new QueryWrapper<>()));
    }

    /**
     * 更改员工时长
     */
    private void changeEmployeeTimes(Times times, double timeNum ,Integer counts){
        times.setTimeSum(times.getTimeSum() + timeNum);
        times.setCounts(times.getCounts() + counts);
    }

    /*
     * 更改员工时长
     */
    private void updateEmployeeTimesByJDBC(ArrayList<Times> timesArrayList){
        for (Times times : timesArrayList) {
            timesMapper.update(times,new QueryWrapper<Times>().eq("employee_ID",times.getEmployeeID())) ;
        }
    }

    /**
     * 查看员工本天的值班次数是否超过了规定水准，以及是否存在值班超时的情况
     */
    public boolean locationIsRegularity( SystemWorkTimeDTO systemWorkTimeDTO , String employeeID
            ,SystemRestTimeDTO systemRestTimeDTO ,List<String> locationIDs,ArrayList<Times> timesArrayList){
        try {
            if (locationIDs.isEmpty()) return false ;
        }catch (Exception ex){
            System.out.println(employeeID);
        }
        String locationID = locationIDs.get(0) ;
        List<Working> workingList = workingMapper.selectList
                (new QueryWrapper<Working>()
                        .like("location_ID", locationID.substring(0,10))
                        .eq("employee_ID",employeeID));
        double dayMaxTime = systemWorkTimeDTO.getDayMaxTime();
        //当日最高工作时长不能超过一定的时长
        if (workingList.size()>dayMaxTime/MIN_WORKINGTIME) return false ;
        //检查是否出现超时工作现象
        double maxFlowNumber = systemWorkTimeDTO.getLocationMaxTime()/MIN_FLOW_TIME ;
        if(isOverTime(maxFlowNumber,workingList,locationIDs,systemRestTimeDTO)) {
            return false ;
        }
        //检查本周的工作时长是否超过了时间限制
        Times times = null ;
        for (Times time : timesArrayList) {
            if (time.getEmployeeID().equals(employeeID)){
                times = time ;
            }
        }
        if (Objects.isNull(times)) return false ;
        return times.getPermitTime() - times.getTimeSum() >= locationIDs.size()*MIN_WORKINGTIME ;
    }

    /**
     * 排班是否违规
     */
    public boolean isOverTime(double maxFlowNumber ,List<Working> workingList ,List<String> locationIDs
            , SystemRestTimeDTO systemRestTimeDTO){
        if (locationIDs.isEmpty()) return false ;
        //将ID号全部存入
        ArrayList<Integer> IDs = new ArrayList<>();
        for (Working working : workingList) {
            IDs.add(Integer.valueOf(working.getLocationID().substring(11,13))) ;
        }
        //检查是否超时和存在不休息的情况
        for (String locationID : locationIDs) {
            try{
                Integer id = Integer.valueOf(locationID.substring(11, 13));
                IDs.add(id) ;
            }
            catch (Exception exception){
                System.out.println(locationID);
            }
        }
        IDs.sort(Comparator.comparingInt(o -> o));
        int count = 0 ;
        for(int i = 0 ; i < IDs.size() - 1 ; i++){
             int j = i+1 ;
             if(IDs.get(i) - IDs.get(j)!=1) {
                 //当连续的班次为8并且前后两个班次小于最小休息时间时则返回false
                 if (IDs.get(i)-IDs.get(j)<systemRestTimeDTO.getRestTime() && count == maxFlowNumber) return false ;
                 count = 0 ;
             } else {
                 //如果班次已经执行了8次，则直接返回false
                 if (count==maxFlowNumber) return false ;
                 count ++ ;
             }
        }

        return count>maxFlowNumber;
    }

    /**
     * 改变员工的优先级
     */
    public void changePriority(PriorityDTO priorityDTO ,double priority){
        priorityDTO.setPriority(priorityDTO.getPriority() + priority);
    }

    /**
     * 按照优先级对员工进行排序
     */
    public void prioritySort(ArrayList<PriorityDTO> priorityDTOList){
        priorityDTOList.sort((o1, o2) -> o1.getPriority() >= o2.getPriority() ? -1 : 1);
    }

    /**
     * 对员工偏好在休息日的员工进行排班
     */
    public boolean manageEmployeeOnWeekDay(ArrayList<PriorityDTO> priorityDTOList
            , ArrayList<EmployeeNumberDTO> employeeNumberDTOList , String employeeID ,EmployeeRoleDTO employeeRoleDTO
            , LocalDateTime localDateTime ,SystemWorkTimeDTO systemWorkTimeDTO ,SystemRestTimeDTO systemRestTimeDTO
            , ArrayList<Times> timesArrayList){
        //依据员工的喜好获取员工的喜欢的locationID
        ArrayList<String> locationIDs = convertEmployeeRole2LocationID(employeeRoleDTO, localDateTime);
        int flag = 1 ;
        //对员工的locationID进行判断
        for (String locationID : locationIDs) {
            String date = locationID.substring(0, 10);
            LocalDateTime time = UsualMethodUtils.StringToChineseLocalDateTime(date);
            //如果时间段是在周六或者是周日，进入如下判断
            if (time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY){
                //获取locationID对应的班次的时间段
                ArrayList<String> approriateLocationIDs = UsualMethodUtils.parseLocation2Locations(locationID);
                for (EmployeeNumberDTO employeeNumberDTO : employeeNumberDTOList) {
                    //对这个时间段的人是否还可以添加
                    ArrayList<String> checkLocations = new ArrayList<>(employeeNumberDTO.getLocationIDs());
                    if(!checkLocations.retainAll(approriateLocationIDs)) {
                        //可以添加，加入进来
                        if (manageLocation(employeeID,approriateLocationIDs,employeeNumberDTO
                                ,systemWorkTimeDTO,systemRestTimeDTO,timesArrayList)){
                            flag = 0 ;
                        }
                    }
                }
            }
        }
        if (flag == 0)  {
            searchPriority(priorityDTOList,employeeID,3);
            return true ;
        }
        searchPriority(priorityDTOList,employeeID,4);
        return false;
    }

    /**
     * 改变指定员工的的优先级
     */
    public void searchPriority(ArrayList<PriorityDTO> priorityDTOList ,String employeeID ,double priority){
        for (PriorityDTO priorityDTO : priorityDTOList) {
            if (priorityDTO.getEmployeeID().equals(employeeID)) {
                changePriority(priorityDTO,priority);
            }
        }
    }

    /**
     * 更新优先级
     */
    public void updatePriority(ArrayList<PriorityDTO> priorityDTOArrayList){
        ArrayList<Priority> priorityArrayList = new ArrayList<>();
        for (PriorityDTO priorityDTO : priorityDTOArrayList) {
            Priority priority = new Priority(priorityDTO.getEmployeeID(), priorityDTO.getPriority());
            priorityArrayList.add(priority) ;
        }
        for (Priority priority : priorityArrayList) {
            priorityMapper.update(priority,new QueryWrapper<Priority>().eq("employee_ID",priority.getEmployeeID())) ;
        }
    }
    //存在一个问题，如果时间段被分开，那么就要按照所占时间段的个数来决定
}
