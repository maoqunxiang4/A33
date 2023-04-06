package com.fuchuang.A33.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fuchuang.A33.DTO.*;
import com.fuchuang.A33.entity.*;
import com.fuchuang.A33.mapper.*;
import com.fuchuang.A33.service.ILocationService;
import com.fuchuang.A33.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fuchuang.A33.utils.Constants.*;
import static com.fuchuang.A33.utils.UsualMethodUtils.*;

@Service
public class LocationServiceImpl implements ILocationService {

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private EmployeeMapper employeeMapper ;

    @Autowired
    private WorkingMapper workingMapper ;

    @Autowired
    private ShopMapper shopMapper ;

    @Autowired
    private EmployeeRoleMapper employeeRoleMapper ;

    @Autowired
    private TimesMapper timesMapper ;

    @Autowired
    private StringRedisTemplate stringRedisTemplate ;

    @Autowired
    private PositionMapper positionMapper ;

    @Autowired
    private ShopRoleMapper shopRoleMapper ;

    @Autowired
    private SystemRoleMapper systemRoleMapper ;

    /**
     * 返回以本周为基准的前后各一个周
     * @param dateTimeWeek
     * @return
     */
    @Override
    public Result getThreeMonthes(String dateTimeWeek) {
        ArrayList<WeeksDTO> weeksDTOList = new ArrayList<>();

        LocalDateTime localDateTime = UsualMethodUtils.StringToChineseLocalDateTime(dateTimeWeek);

        LocalDateTime today = UsualMethodUtils.parseToMonday(localDateTime);
        LocalDateTime lastThreeMonthes = today.minusMonths(3).minusDays(1);
        LocalDateTime nextThreeMonthes = today.plusMonths(3).minusDays(1);
        LocalDateTime time = lastThreeMonthes ;

        int count = 1 ;
        String counts = "第" + count + "周" ;
        while(time.isBefore(nextThreeMonthes.plusWeeks(1))){
            String date = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int start = time.getDayOfMonth();
            LocalDateTime nextWeek = time.plusWeeks(1).minusDays(1);
            int end = nextWeek.getDayOfMonth();

            WeeksDTO weeksDTO = new WeeksDTO();
            weeksDTO.setCounts(counts);
            weeksDTO.setWeek(date);
            weeksDTO.setStartDay(start>=10 ? String.valueOf(start) : "0" + start);
            weeksDTO.setEndDay(end>=10 ? String.valueOf(end) : "0" + end);
            count++ ;
            time = time.plusWeeks(1) ;
            counts = "第" + count + "周" ;
            weeksDTOList.add(weeksDTO) ;
        }
        return Result.success(200,weeksDTOList);
    }

    /**
     * 得到本周的星期一的日期
     * @return
     */
    @Override
    public Result getMondayThisWeek() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        while(dayOfWeek != DayOfWeek.MONDAY){
            now = now.minusDays(1);
            dayOfWeek = now.getDayOfWeek() ;
        }
        String Monday = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return Result.success(200,Monday);
    }

    /**
     * 按周进行查看
     * @param dateTimeWeek
     * @return
     */
    @Override
    public Result showAllLocationsByWeek(String dateTimeWeek) {
        //对字符串进行解析
        LocalDateTime localDateTime = UsualMethodUtils.StringToChineseLocalDateTime(dateTimeWeek) ;

        //判断是否是星期一
        if (localDateTime.getDayOfWeek()!=DayOfWeek.MONDAY){
            localDateTime = UsualMethodUtils.parseToMonday(localDateTime) ;
        }
        ArrayList<WorkingDTO> workingDTOList1 = new ArrayList<>();
        ArrayList<List<WorkingDTO>> workingDTOList2 = new ArrayList<>() ;
        ArrayList<List<List<WorkingDTO>>> workingDTOList3 = new ArrayList<>() ;

        //对本周的值班情况进行查询
        for(int i = 0 ; i < 7 ; i++){
            //将天数进行转化
            UsualMethodUtils.getOneDayAboutWorkingDTOListByLocationList(workingMapper ,workingDTOList1
                    ,workingDTOList2 , localDateTime , locationMapper) ;
            workingDTOList3.add(workingDTOList2) ;
            workingDTOList2 = new ArrayList<>() ;
            //天数加1
            localDateTime = localDateTime.plusDays(1) ;
        }

        return Result.success(200, workingDTOList3);
    }

    /**
     * 按日进行查看
     * @param dateTimeDay
     * @return
     */
    @Override
    public Result showAllLocationsByDay(String dateTimeDay) {
        //同上述用法一致
        List<Location> locationList = locationMapper.selectList(new QueryWrapper<Location>().like("ID", dateTimeDay));
        ArrayList<WorkingDTO> workingDTOList1 = new ArrayList<>();
        ArrayList<List<WorkingDTO>> workingDTOList2 = new ArrayList<>();

        LocalDateTime localDateTime = UsualMethodUtils.StringToChineseLocalDateTime(dateTimeDay);
        UsualMethodUtils.getOneDayAboutWorkingDTOListByLocationList(workingMapper ,workingDTOList1
                ,workingDTOList2 , localDateTime , locationMapper) ;

        return Result.success(200, workingDTOList2);
    }

    /**
     * 展示所有的组别信息，我们在这里面只展示小组长的姓名，依据小组长的ID号进行分组
     * @return
     */
    @Override
    public Result showAllGroup() {
        List<Employee> employees = employeeMapper.selectList(new QueryWrapper<Employee>().eq("position", "小组长"));
        ArrayList<EmployeeDTO> employeeDTOS = new ArrayList<>();
        for (Employee employee : employees) {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            BeanUtil.copyProperties(employee,employeeDTO);
            employeeDTOS.add(employeeDTO) ;
        }
        return Result.success(200,employeeDTOS);
    }

    /**
     * 按照小组的方式对员工的班次进行展示
     */
    @Override
    public Result showAllLocationsByGroup(String groupID , String dateTime) {
        ArrayList<WorkingDTO> workingDTOList1 = new ArrayList<>();
        ArrayList<List<WorkingDTO>> workingDTOList2 = new ArrayList<>();
        ArrayList<List<List<WorkingDTO>>> workingDTOList3 = new ArrayList<>();

        List<Employee> employeeList = employeeMapper.selectList(new QueryWrapper<Employee>().eq("belong", groupID));
        Employee em = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", groupID));
        employeeList.add(em) ;
        getGroupLocationListByWorkingList(employeeList, workingMapper, locationMapper
                ,dateTime , workingDTOList1 ,workingDTOList2 ,workingDTOList3);
        return Result.success(200,workingDTOList3);
    }

    /**
     * 展示员工具体细节信息，通过封装员工信息得到
     */
    @Override
    public ResultWithToken showEmployeeDetails(String employeeID) {
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);
        Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", employeeID));
        if (Objects.isNull(employee)){
            return ResultWithToken.fail(500,"the employee is not excite now , please sure it now") ;
        }
        if (employee.getPosition().equals("root")) return ResultWithToken.fail(500,"can not search user named 'root' ") ;
        //对需要返回的具体员工信息封装到EmployeeDetails类中
        EmployeeDetailsInformationDTO employeeDetailsInformationDTO = new EmployeeDetailsInformationDTO();
        employeeDetailsInformationDTO.setEmail(employee.getEmail());
        Employee group = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", employee.getID()));

        if (Objects.isNull(group))  employeeDetailsInformationDTO.setGroupName("无");
        else employeeDetailsInformationDTO.setGroupName(group.getName());

        employeeDetailsInformationDTO.setPosition(employee.getPosition());
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("ID", employee.getShopID()));

        if (Objects.isNull(shop)) employeeDetailsInformationDTO.setShopName("无") ;
        else employeeDetailsInformationDTO.setShopName(shop.getName());

        employeeDetailsInformationDTO.setID(employee.getID());
        employeeDetailsInformationDTO.setName(employee.getName());
        employeeDetailsInformationDTO.setPhone(employee.getPhone());
        //添加员工喜好
        List<EmployeeRole> employeeRoleList = employeeRoleMapper.selectList
                (new QueryWrapper<EmployeeRole>().eq("employee_ID", employeeID));
            //将员工喜好依次加入，对于其中为null的部分，我们手动赋值为空，并在数据表中进行修改
        for (EmployeeRole employeeRole : employeeRoleList) {
            switch (employeeRole.getHobbyType()){
                case "工作日偏好" : {
                    if (employeeRole.getHobbyValue().equals("")){
                        employeeDetailsInformationDTO.setEmployeeWorkDayDTO(null);
                        EmployeeRole role = new EmployeeRole();
                        role.setHobbyValue(DEFAULT_HOBBY_VALUE);
                        employeeRoleMapper.update(role,new UpdateWrapper<EmployeeRole>()
                                .eq("hobby_type","工作日偏好")
                                .eq("employee_ID",employeeID)) ;
                        break;
                    }
                    employeeDetailsInformationDTO
                            .setEmployeeWorkDayDTO(roleUtils.parseEmployeeRole(EmployeeWorkDayDTO.class ,EMPLOYEEROLE_TYPE1
                                    , employee.getID()));
                    break;
                }

                case "工作时间偏好" : {
                    if (employeeRole.getHobbyValue().equals("")){
                        employeeDetailsInformationDTO.setEmployeeWorkTimeDTO(null);
                        EmployeeRole role = new EmployeeRole();
                        role.setHobbyValue(DEFAULT_HOBBY_VALUE);
                        employeeRoleMapper.update(role,new UpdateWrapper<EmployeeRole>()
                                .eq("hobby_type","工作时间偏好")
                                .eq("employee_ID",employeeID)) ;
                        break;
                    }
                    employeeDetailsInformationDTO.setEmployeeLastTimeDTO(roleUtils.parseEmployeeRole(EmployeeLastTimeDTO.class ,EMPLOYEEROLE_TYPE3
                            , employee.getID()));
                    break;
                }

                case "班次时长偏好" : {
                    if (employeeRole.getHobbyValue().equals("")){
                        employeeDetailsInformationDTO.setEmployeeLastTimeDTO(null);
                        EmployeeRole role = new EmployeeRole();
                        role.setHobbyValue(DEFAULT_HOBBY_VALUE);
                        employeeRoleMapper.update(role,new UpdateWrapper<EmployeeRole>()
                                .eq("hobby_type","班次时长偏好")
                                .eq("employee_ID",employeeID)) ;
                        break;
                    }
                    employeeDetailsInformationDTO.setEmployeeLastTimeDTO(roleUtils.parseEmployeeRole(EmployeeLastTimeDTO.class ,EMPLOYEEROLE_TYPE3
                            , employee.getID()));
                    break;
                }
            }
        }
        String token = stringRedisTemplate.opsForValue().get(GET_TOKEN + employee.getID());
        return ResultWithToken.success(200, employeeDetailsInformationDTO,token);
    }

    /**
     * 手动安排员工班次
     */
    @Override
    public Result manageEmployeeLocationsByHand( String employeeID , String... locationIDList)
    {
        ArrayList<Working> workingList = new ArrayList<>();
        for (String locationID : locationIDList) {

            if (locationID.length()!=13)
                return Result.fail(500,"the input is not right") ;
            //根据员工ID号进行查询，将得到的内容填充到working中
            Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", employeeID));
            if (Objects.isNull(employee)) return Result.fail(500,"employee is not excite");
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
                throw new RuntimeException("this employee has exited here now") ;

            //根据用户选中的班次ID进行查询
            Location location = locationMapper.selectOne(new QueryWrapper<Location>().eq("ID", locationID));
            Location newLocation = new Location();
            //如果查询结果为空，说明该班次没有人，则将current_num赋值为1；否则，在current_num的基础上加1
            if (Objects.isNull(location)) {
                newLocation.setID(locationID);
                newLocation.setCurrentNumber(1);
                String flowID = UsualMethodUtils.getRealFlowID(locationID);
                newLocation.setFlowID(flowID);
                int rows =  locationMapper.insert(newLocation) ;
                if (rows!=1)  return Result.fail(500,"please try it again") ;
            }else{
                newLocation.setID(locationID);
                newLocation.setCurrentNumber(location.getCurrentNumber()+1);
                newLocation.setFlowID(UsualMethodUtils.getRealFlowID(locationID));
                int rows = locationMapper.update(newLocation,new UpdateWrapper<Location>().eq("ID",locationID)) ;
            }

            //存入
            workingList.add(working) ;
        }
        int rows = 0 ;
        for (Working working : workingList) {
            rows = workingMapper.insert(working) ;
        }
        int size = workingList.size();
        if (rows!=1)  return Result.fail(500,"please try it again") ;
        Times times = timesMapper.selectOne(new QueryWrapper<Times>().eq("employee_ID",employeeID));
        int counts = times.getCounts() + 1 ;
        double timeSum = counts * MIN_WORKINGTIME ;
        Times time = new Times();
        time.setCounts(counts);
        time.setTimeSum(timeSum);
        rows = timesMapper.update(time, new QueryWrapper<Times>().eq("employee_ID", employeeID));
        if (rows!=1)  return Result.fail(500,"please try it again") ;
        return Result.success(200);
    }

    //location的表只能增加不能删除
    /**
     * 手动移除班次
     */
    @Override
    public Result removeLocationsByHand (String employeeID ,String... locationIDList) {
        String IDs = "" ;
        for (int i = 0 ; i < locationIDList.length ; i++) {
             if(locationIDList.length-1 != i) IDs =  IDs + "\'"  + locationIDList[i] + '\'' + "," ;
             else IDs = IDs + '\'' + locationIDList[i] + '\'' ;
        }
//        ArrayList<Object> IDs = new ArrayList<>(Arrays.asList(locationIDList));
//        List<Working> workings = workingMapper.selectList(new QueryWrapper<Working>().inSql("employee_ID", IDs));
//        int counts = workings.size() ;
        Long counts = workingMapper.selectCount(new QueryWrapper<Working>().inSql("location_ID", IDs));

        if (counts != locationIDList.length)
            return Result.fail(500,"some locations has not excite , please flush the page again") ;

        int rows = workingMapper.delete(new QueryWrapper<Working>()
                .inSql("location_ID", IDs)
                .eq("employee_ID",employeeID));
        if (rows==0){
            return Result.fail(500,"the location is not excite now , please donnot delete agein " );
        }

        rows = locationMapper.update(new Location(),new UpdateWrapper<Location>()
                        .inSql("ID",IDs)
                .setSql("current_number = current_number - 1"));
        if (rows==0){
            return Result.fail(500,"the location is not excite now , please donnot delete agein " );
        }


        Times times = timesMapper.selectOne(new QueryWrapper<Times>().eq("employee_ID",employeeID));
        int count = times.getCounts() - locationIDList.length ;
        double timeSum = count * MIN_WORKINGTIME ;
        Times time = new Times();
        time.setCounts(count);
        time.setTimeSum(timeSum);
        rows = timesMapper.update(time, new QueryWrapper<Times>().eq("employee_ID", employeeID));
        if (rows==0){
            return Result.fail(500,"the system has some wrongs") ;
        }
        return Result.success(200);
    }

    /**
     * 通过姓名展示用户
     */
    @Override
    public Result showEmployeeByName(String name) {
        List<Employee> employeeList = employeeMapper.selectList(new QueryWrapper<Employee>().eq("name", name));
        return Result.success(200,employeeList);
    }

    /**
     * 通过email展示班次信息，与前面的showEmployeeByName搭配使用
     */
    @Override
    public Result showEmployeeLocationsByEmail(String dateTime , String email) {
        LocalDateTime localDateTime = UsualMethodUtils.StringToChineseLocalDateTime(dateTime) ;
        Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("email", email));
        //判断是否是星期一
        if (localDateTime.getDayOfWeek()!=DayOfWeek.MONDAY){
            localDateTime = UsualMethodUtils.parseToMonday(localDateTime) ;
        }
        ArrayList<WorkingDTO> workingDTOList = new ArrayList<>();
        ArrayList<List<WorkingDTO>> list = new ArrayList<>();
        //对本周的值班情况进行查询
        for(int i = 0 ; i < 7 ; i++){
            //将天数进行转化
            String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Location> locationList = locationMapper.selectList(new QueryWrapper<Location>().like("ID",date));
            for (Location location : locationList) {
                List<Working> workingList = workingMapper.selectList(
                        new QueryWrapper<Working>()
                                .eq("location_ID", location.getID())
                                .eq("employee_ID", employee.getID()));
                for (Working working : workingList) {
                    WorkingDTO workingDTO = new WorkingDTO();
                    //将结果转换成locationDTO对象
                    BeanUtil.copyProperties(working, workingDTO);
                    workingDTO.setLocationRealID(location.getID().substring(11));
                    workingDTOList.add(workingDTO) ;
                }
            }
            list.add(workingDTOList) ;
            workingDTOList = new ArrayList<>();
            //天数加1
            localDateTime = localDateTime.plusDays(1) ;
        }
        return Result.success(200,list);
    }

    /**
     * 当员工的工作时长没有到达要求的时候就会进行展示（root用户不能访问）
     */
    @Override
    public Result showFreeEmployees() {
        EmployeeDTO em = EmployeeHolder.getEmloyee();
        String employeeID = em.getID();
        List<Employee> employeeList = employeeMapper.selectList(new QueryWrapper<Employee>()
                .eq("shop_ID", em.getShopID()));
        ArrayList<Employee> employees = new ArrayList<>();
        if (!employeeList.isEmpty()){
            for (Employee employee : employeeList) {
                Times times = timesMapper.selectOne(new QueryWrapper<Times>().eq("employee_ID", employeeID));
                if ( times.getPermitTime() - times.getTimeSum() >= Constants.MIN_WORKINGTIME ){
                    employees.add(employee) ;
                }
            }
        }

        return Result.success(200,employees);
    }

    @Override
    public Result showEmployeeLocationsByPosition(String position , String dateTime) {
        ArrayList<WorkingDTO> workingDTOList1 = new ArrayList<>();
        ArrayList<List<WorkingDTO>> workingDTOList2 = new ArrayList<>();
        ArrayList<List<List<WorkingDTO>>> workingDTOList3 = new ArrayList<>();

        if (position.equals("root")){
            return Result.fail(500,"root用户不能被查询") ;
        }
        ArrayList<String> list = new ArrayList<>();
        list.add("店长") ;list.add("经理") ;list.add("小组长") ;
        ArrayList<ArrayList<ArrayList<WorkingDTO>>> workingDTOS = new ArrayList<>() ;
        if (list.contains(position)) {
            List<Employee> employees = employeeMapper.selectList(new QueryWrapper<Employee>()
                    .eq("position", position)
                    .eq("shop_ID", EmployeeHolder.getEmloyee().getShopID()));
            getGroupLocationListByWorkingList(employees, workingMapper, locationMapper, dateTime
                    , workingDTOList1 ,workingDTOList2 ,workingDTOList3);
        }else {
            List<Employee> employees = employeeMapper.selectList(new QueryWrapper<Employee>()
                    .ne("position", list.get(0))
                    .ne("position", list.get(1))
                    .ne("position", list.get(2))
                    .eq("shop_ID", EmployeeHolder.getEmloyee().getShopID()));
            getGroupLocationListByWorkingList(employees, workingMapper, locationMapper, dateTime
                    , workingDTOList1 ,workingDTOList2 ,workingDTOList3);
        }
        return Result.success(200,workingDTOList3);
    }


    @Override
    public Result getPositions() {
        List<Position> positions = positionMapper.selectList(new QueryWrapper<Position>());
        return Result.success(200,positions);
    }

    @Override
    public Result showLocationDetails(String locationID) {
        List<Location> locationList = locationMapper.selectList(new QueryWrapper<Location>().eq("ID", locationID));
        ArrayList<WorkingDTO> workingDTOList = new ArrayList<>();
        for (Location location : locationList) {
            Working working =
                    workingMapper.selectOne(new QueryWrapper<Working>().eq("location_ID", location.getID()));
            WorkingDTO workingDTO = new WorkingDTO();
            BeanUtil.copyProperties(working,workingDTO);
            workingDTOList.add(workingDTO) ;
            workingDTO.setLocationRealID(workingDTO.getLocationID().substring(11,13));
        }
        return Result.success(200,workingDTOList);
    }

    @Override
    public Result showWeekLocationOfOne(String dateTimeWeek) {
        String employeeID = EmployeeHolder.getEmloyee().getID();
        ArrayList<WorkingDTO> workingDTOList1 = new ArrayList<>();
        ArrayList<List<WorkingDTO>> workingDTOList2 = new ArrayList<>();
        ArrayList<List<List<WorkingDTO>>> workingDTOList3 = new ArrayList<>();
        LocalDateTime localDateTime = StringToChineseLocalDateTime(dateTimeWeek);

        List<Location> locationList = locationMapper.selectList(new QueryWrapper<Location>()
                .likeRight("ID", localDateTime));
        for (int i = 0; i < 7; i++) {
            getSomeoneWorkingDTOListByLocationList(workingMapper, workingDTOList1,
                     workingDTOList2 , localDateTime, locationMapper , employeeID) ;
        }
        getWorkingDTOListByLocationList(workingMapper,locationList,workingDTOList1,workingDTOList2) ;
        workingDTOList3.add(workingDTOList2);
        return Result.success(200,workingDTOList3);
    }

    @Override
    public Result showDayLocationOfOne(String dateTimeDay) {
        String employeeID = EmployeeHolder.getEmloyee().getID();
        ArrayList<WorkingDTO> workingDTOList1 = new ArrayList<>();
        ArrayList<List<WorkingDTO>> workingDTOList2 = new ArrayList<>();
        ArrayList<List<List<WorkingDTO>>> workingDTOList3 = new ArrayList<>();
        LocalDateTime localDateTime = StringToChineseLocalDateTime(dateTimeDay);

        getSomeoneWorkingDTOListByLocationList( workingMapper, workingDTOList1,  workingDTOList2
                ,  localDateTime,  locationMapper , employeeID) ;
        workingDTOList3.add(workingDTOList2) ;
        return Result.success(200,workingDTOList3);
    }

}