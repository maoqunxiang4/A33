package com.fuchuang.A33.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuchuang.A33.DTO.*;
import com.fuchuang.A33.entity.ShopRole;
import com.fuchuang.A33.entity.*;
import com.fuchuang.A33.mapper.*;
import com.fuchuang.A33.service.IShopRoleService;
import com.fuchuang.A33.utils.EmployeeHolder;
import com.fuchuang.A33.utils.Result;
import com.fuchuang.A33.utils.RoleUtils;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.RespectBindingFeature;
import java.util.*;

import static com.fuchuang.A33.utils.Constants.*;
import static com.fuchuang.A33.utils.RoleUtils.*;
import static javax.xml.ws.RespectBindingFeature.ID;

@Service
public class ShopRoleServiceImpl implements IShopRoleService {
    @Autowired
    private ShopRoleMapper shopRoleMapper ;

    @Autowired
    private ShopMapper shopMapper ;

    @Autowired
    private SystemRoleMapper systemRoleMapper ;

    @Autowired
    private TimesMapper timesMapper ;

    @Autowired
    private EmployeeMapper employeeMapper ;

    @Autowired
    private EmployeeRoleMapper employeeRoleMapper ;

    @Override
    public Result addShop(String name, String address, double size) {
        Long count = shopMapper.selectCount(new QueryWrapper<Shop>());
        String ID = null ;
        if (count<=8) ID = "0" + (count + 1) + "" ;
        else ID = count + 1 + "" ;
        shopMapper.insert(new Shop(ID,name,address,size)) ;
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper);

        //添加默认系统规则
        SystemRole systemRole = new SystemRole();
        systemRole.setShopID(ID);
        systemRole.setSystemRoleType(SYSTEM_LAST_TIME);
        systemRole.setSystemRoleValue(roleUtils.getInitSystemLastTimeDTO());
        systemRole.setComment("\"1~5,9,21,6,7,10,22\"，分别表示工作日周1~周5，从9点开始工作，21点下班，周6周7是10点开始上班，22点下班");
        systemRoleMapper.insert(systemRole) ;
        systemRole.setShopID(ID);
        systemRole.setSystemRoleType(SYSTEM_WORK_TIME);
        systemRole.setSystemRoleValue(roleUtils.getInitSystemWorkTimeDTO());
        systemRole.setComment("\"40,8,2,4\"，员工单周最长工作时间是40小时，单日最长工作时间为8小时，单次工作时间不得小于两小时，不得高于4小时");
        systemRoleMapper.insert(systemRole) ;
        systemRole.setShopID(ID);
        systemRole.setSystemRoleType(SYSTEM_REST_TIME);
        systemRole.setSystemRoleValue(roleUtils.getInitSystemRestTimeDTO());
        systemRole.setComment("每个员工的休息时长");
        systemRoleMapper.insert(systemRole) ;
        systemRole.setShopID(ID);
        systemRole.setSystemRoleType(SYSTEM_GROUP_POSITION);
        systemRole.setSystemRoleValue(roleUtils.getInitSystemGroupPositionDTO());
        systemRole.setComment("各个职位在人数到达一定上限之后，各个职位的分组");
        systemRoleMapper.insert(systemRole) ;

        //添加用户自定义规则
        ShopRole shopRole = new ShopRole();
        shopRole.setShopID(ID);
        shopRole.setShopRoleType(OPEN_ROLE);
        shopRole.setShopRoleValue(roleUtils.getInitOpenRole(size));
        shopRole.setComment("\"1.5,23.5\" 表示开店1个半小时前需要有员工当值，当值员工数为门店面积除以23.5,fomual表示最后当值员工数");
        shopRoleMapper.insert(shopRole) ;
        shopRole.setShopID(ID);
        shopRole.setShopRoleType(CLOSE_ROLE);
        shopRole.setShopRoleValue(roleUtils.getInitCloseRole(size));
        shopRole.setComment("\"2.5,3,13\" 表示关店两个半小时内需要有员工当值，当值员工数不小于3并且不小于门店面积除以13,fomual表示最后当值员工数");
        shopRoleMapper.insert(shopRole) ;
        shopRole.setShopID(ID);
        shopRole.setShopRoleType(FLOW_ROLE);
        shopRole.setShopRoleValue(roleUtils.getInitFlowRole());
        shopRole.setComment("\"3.8\"  表示按照业务预测数据，每3.8个客流必须安排至少一个员工当值");
        shopRoleMapper.insert(shopRole) ;
        shopRole.setShopID(ID);
        shopRole.setShopRoleType(GROUP_ROLE);
        shopRole.setShopRoleValue(roleUtils.getInitGroupRole());
        shopRole.setComment("表示每n个员工需要有一个小组长");
        shopRoleMapper.insert(shopRole) ;
        shopRole.setShopID(ID);
        shopRole.setShopRoleType(STOCK_ROLE);
        shopRole.setShopRoleValue(roleUtils.getInitStockRole());
        shopRole.setComment("\"5,2,4\",表示进货时至少需要5个员工，进货时长不长于2小时，不短于4小时");
        shopRoleMapper.insert(shopRole) ;
        return Result.success(200);
    }

    @Override
    public Result showSystemRole() {
        String ID = EmployeeHolder.getEmloyee().getShopID();
        List<SystemRole> systemRoleList = systemRoleMapper.selectList(new QueryWrapper<SystemRole>().eq("shop_ID", ID));
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper);
        if (systemRoleList.isEmpty()){
            SystemRole systemRole = new SystemRole();
            systemRole.setShopID(ID);
            systemRole.setSystemRoleType(SYSTEM_LAST_TIME);
            systemRole.setSystemRoleValue(roleUtils.getInitSystemLastTimeDTO());
            systemRole.setComment("\"1~5,9,21,6,7,10,22\"，分别表示工作日周1~周5，从9点开始工作，21点下班，周6周7是10点开始上班，22点下班");
            systemRoleMapper.insert(systemRole) ;
            systemRole.setShopID(ID);
            systemRole.setSystemRoleType(SYSTEM_WORK_TIME);
            systemRole.setSystemRoleValue(roleUtils.getInitSystemWorkTimeDTO());
            systemRole.setComment("\"40,8,2,4\"，员工单周最长工作时间是40小时，单日最长工作时间为8小时，单次工作时间不得小于两小时，不得高于4小时");
            systemRoleMapper.insert(systemRole) ;
            systemRole.setShopID(ID);
            systemRole.setSystemRoleType(SYSTEM_REST_TIME);
            systemRole.setSystemRoleValue(roleUtils.getInitSystemRestTimeDTO());
            systemRole.setComment("每个员工的休息时长");
            systemRoleMapper.insert(systemRole) ;
            systemRole.setShopID(ID);
            systemRole.setSystemRoleType(SYSTEM_GROUP_POSITION);
            systemRole.setSystemRoleValue(roleUtils.getInitSystemGroupPositionDTO());
            systemRole.setComment("各个职位在人数到达一定上限之后，各个职位的分组");
            systemRoleMapper.insert(systemRole) ;
            systemRoleList = systemRoleMapper.selectList(new QueryWrapper<SystemRole>().eq("shop_ID", ID));
        }
        return Result.success(200,systemRoleList);
    }

    @Override
    public Result showShopRoleAndShopValue() {
        EmployeeDTO emloyee = EmployeeHolder.getEmloyee();
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("ID", emloyee.getShopID()));
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper);
        List<ShopRole> shopRoleList = shopRoleMapper.selectList(new QueryWrapper<ShopRole>()
                .eq("shop_ID", emloyee.getShopID()));
        String ID = emloyee.getShopID();
        double size = shop.getSize();
        if (shopRoleList.isEmpty()){
            //添加用户自定义规则
            ShopRole shopRole = new ShopRole();
            shopRole.setShopID(ID);
            shopRole.setShopRoleType(OPEN_ROLE);
            shopRole.setShopRoleValue(roleUtils.getInitOpenRole(size));
            shopRole.setComment("\"1.5,23.5\" 表示开店1个半小时前需要有员工当值，当值员工数为门店面积除以23.5,fomual表示最后当值员工数");
            shopRoleMapper.insert(shopRole) ;
            shopRole.setShopID(ID);
            shopRole.setShopRoleType(CLOSE_ROLE);
            shopRole.setShopRoleValue(roleUtils.getInitCloseRole(size));
            shopRole.setComment("\"2.5,3,13\" 表示关店两个半小时内需要有员工当值，当值员工数不小于3并且不小于门店面积除以13,fomual表示最后当值员工数");
            shopRoleMapper.insert(shopRole) ;
            shopRole.setShopID(ID);
            shopRole.setShopRoleType(FLOW_ROLE);
            shopRole.setShopRoleValue(roleUtils.getInitFlowRole());
            shopRole.setComment("\"3.8\"  表示按照业务预测数据，每3.8个客流必须安排至少一个员工当值");
            shopRoleMapper.insert(shopRole) ;
            shopRole.setShopID(ID);
            shopRole.setShopRoleType(GROUP_ROLE);
            shopRole.setShopRoleValue(roleUtils.getInitGroupRole());
            shopRole.setComment("表示每n个员工需要有一个小组长");
            shopRoleMapper.insert(shopRole) ;
            shopRole.setShopID(ID);
            shopRole.setShopRoleType(STOCK_ROLE);
            shopRole.setShopRoleValue(roleUtils.getInitStockRole());
            shopRole.setComment("\"5,2,4\",表示进货时至少需要5个员工，进货时长不长于2小时，不短于4小时");
            shopRoleMapper.insert(shopRole) ;
            shopRoleList = shopRoleMapper.selectList(new QueryWrapper<ShopRole>()
                    .eq("shop_ID", emloyee.getShopID()));
        }
        return Result.success(200,shopRoleList);
    }

    @Override
    public Result updateFlowRole(double baseNum) {
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveFLowRole(new FlowRoleDTO(baseNum) , EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }

    @Override
    public Result updateCloseRole(double endTime, Integer minEmployee, double baseNum, double fomula) {
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveCloseRole(new CloseRoleDTO(endTime,minEmployee,baseNum,fomula) , EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }


    public Result updateOpenRole(double openTime, double baseNum, double fomula) {
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveOpenRole(new OpenRoleDTO(openTime,baseNum,fomula) , EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }

    @Override
    public Result updateGroupRole(int deadLineEmployee) {
        String shopID = EmployeeHolder.getEmloyee().getShopID();
        AllRoleDTO allRoleDTO = new AllRoleDTO(shopID, shopRoleMapper, systemRoleMapper);
        SystemGroupPositionDTO systemGroupPositionDTO = allRoleDTO.getSystemGroupPositionDTO();
        HashMap<String, Integer> positionMap = systemGroupPositionDTO.getPositionMap();
        int sum = 0;
        for (Map.Entry<String, Integer> entry : positionMap.entrySet()) {
            sum += entry.getValue() ;
        }
        if (deadLineEmployee < sum){
            return Result.fail(500,"人数过少，必须大于系统最小值:" + sum) ;
        }
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveGroupRole(new GroupRoleDTO(deadLineEmployee),EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }

    @Override
    public Result updateStockRole(int minEmployee, double minLastTime, double maxLastTime) {
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveStockRole(new StockRoleDTO(minEmployee,minLastTime,maxLastTime) ,EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }

    @Override
    public Result updateLastTime(String workDay, double workStart, double workEnd,
                                 String weekend, double weekendStart, double weekendEnd) {
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveSystemLastTime(new SystemLastTimeDTO
                (workDay,workStart,workEnd,weekend,weekendStart,weekendEnd), EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }

    @Override
    public Result updateRestTime(double lunchTime, double lunchTimeStart, double lunchTimeEnd
            , double dinnerTime, double dinnerTimeStart, double dinnerTimeEnd, double restTime) {
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveSystemRestTime(new SystemRestTimeDTO
                (lunchTime,lunchTimeStart,lunchTimeEnd,dinnerTime,dinnerTimeStart,dinnerTimeEnd,restTime)
                ,EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }

    @Override
    public Result updateWorkTime(double weekMaxTime, double dayMaxTime, double locationMinTime, double locationMaxTime) {
        new RoleUtils(shopRoleMapper, systemRoleMapper).saveSystemWorkTime(new SystemWorkTimeDTO(weekMaxTime,dayMaxTime,locationMinTime,locationMaxTime)
                    ,EmployeeHolder.getEmloyee().getShopID());
        List<Employee> employeeList = employeeMapper.selectList(new QueryWrapper<Employee>()
                .eq("shop_ID", EmployeeHolder.getEmloyee().getShopID()));
        Times times = new Times();
        times.setPermitTime(weekMaxTime);
        for (Employee employee : employeeList) {
            timesMapper.update(times ,new QueryWrapper<Times>().eq("employee_ID",employee.getID())) ;
        }
        return Result.success(200);
    }

    @Override
    public Result updateGroupPosition(String number, String[] position) {

        SystemGroupPositionDTO systemGroupPositionDTO = new SystemGroupPositionDTO();
        String[] positonNumStr = number.split(",");
        if (position.length!=positonNumStr.length) return Result.fail(500,"please input again") ;
        ArrayList<Integer> positionNum = new ArrayList<>();
        for (String numberStr : positonNumStr) {
            positionNum.add(Integer.valueOf(numberStr)) ;
        }
        int count = 0;
        HashMap<String, Integer> map = new HashMap<>();
        for (Integer num : positionNum) {
            map.put(position[count++], num) ;
        }
        ArrayList<String> positions = new ArrayList<>(Arrays.asList(position));
        systemGroupPositionDTO.setPositions(positions);
        systemGroupPositionDTO.setPositionMap(map);
        new RoleUtils(shopRoleMapper, systemRoleMapper)
                .saveSystemGroupPosition(systemGroupPositionDTO,EmployeeHolder.getEmloyee().getShopID());
        return Result.success(200);
    }
}
