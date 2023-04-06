package com.fuchuang.A33.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuchuang.A33.DTO.*;
import com.fuchuang.A33.entity.EmployeeRole;
import com.fuchuang.A33.entity.ShopRole;
import com.fuchuang.A33.entity.SystemRole;
import com.fuchuang.A33.mapper.EmployeeRoleMapper;
import com.fuchuang.A33.mapper.ShopRoleMapper;
import com.fuchuang.A33.mapper.SystemRoleMapper;

import java.util.ArrayList;
import java.util.HashMap;

import static com.fuchuang.A33.utils.Constants.*;
import static com.fuchuang.A33.utils.Constants.GROUP_ROLE;

public class RoleUtils {

    public RoleUtils(ShopRoleMapper shopRoleMapper ,SystemRoleMapper systemRoleMapper) {
        this.shopRoleMapper = shopRoleMapper ;
        this.systemRoleMapper = systemRoleMapper ;
    }

    public RoleUtils(ShopRoleMapper shopRoleMapper ,SystemRoleMapper systemRoleMapper
            , EmployeeRoleMapper employeeRoleMapper) {
        this.shopRoleMapper = shopRoleMapper ;
        this.systemRoleMapper = systemRoleMapper ;
        this.employeeRoleMapper = employeeRoleMapper ;
    }

    private ShopRoleMapper shopRoleMapper ;
    private SystemRoleMapper systemRoleMapper ;
    private EmployeeRoleMapper employeeRoleMapper ;

    /**
     * 保存开店规则
     * @param OpenRole
     * @param shopID
     */
    public  void saveOpenRole(OpenRoleDTO OpenRole , String shopID){
        String OpenRoleJsonStr = JSONUtil.toJsonStr(OpenRole);
        ShopRole shopRole = new ShopRole();
        shopRole.setShopRoleValue(OpenRoleJsonStr);
        shopRoleMapper.update(shopRole ,new QueryWrapper<ShopRole>()
                .eq("shop_role_type",OPEN_ROLE)
                .eq("shop_ID" , shopID)) ;
    }

    /**
     * 解析开店规则
     * @param shopID
     */
    public OpenRoleDTO parseOpenRole(String shopID){
        ShopRole shopRole = shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                .eq("shop_role_type", OPEN_ROLE)
                .eq("shop_ID", shopID)) ;
        String OpenRoleJsonStr = shopRole.getShopRoleValue() ;
        return JSONUtil.toBean(OpenRoleJsonStr, OpenRoleDTO.class) ;
    }

    /**
     * 获取初始化的开店规则
     * @param size
     * @return
     */
    public  String getInitOpenRole(double size){
        OpenRoleDTO openRoleDTO = new OpenRoleDTO();
        openRoleDTO.setOpenTime(1.5);
        openRoleDTO.setBaseNum(23.5);
        openRoleDTO.setFomula(size/23.5);
        return JSONUtil.toJsonStr(openRoleDTO) ;
    }

    /**
     * 保存关店规则
     * @param closeRole
     * @param shopID
     */
    public  void saveCloseRole(CloseRoleDTO closeRole , String shopID){
        String closeRoleJsonStr = JSONUtil.toJsonStr(closeRole);
        ShopRole shopRole = new ShopRole();
        shopRole.setShopRoleValue(closeRoleJsonStr);
        shopRoleMapper.update(shopRole ,new QueryWrapper<ShopRole>()
                .eq("shop_role_type",CLOSE_ROLE)
                .eq("shop_ID" , shopID)) ;
    }

    /**
     * 解析关店规则
     * @param shopID
     */
    public  CloseRoleDTO parseCloseRole(String shopID){
        ShopRole shopRole = shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                .eq("shop_role_type", CLOSE_ROLE)
                .eq("shop_ID", shopID)) ;
        String closeRoleJsonStr = shopRole.getShopRoleValue() ;
        return JSONUtil.toBean(closeRoleJsonStr, CloseRoleDTO.class) ;
    }

    /**
     * 获取初始化的关店规则
     * @param size
     * @return
     */
    public  String getInitCloseRole(double size){
        CloseRoleDTO closeRoleDTO = new CloseRoleDTO();
        closeRoleDTO.setEndTime(2.5);
        closeRoleDTO.setMinEmployee(3);
        closeRoleDTO.setBaseNum(13);
        closeRoleDTO.setFomula(size/13);
        return JSONUtil.toJsonStr(closeRoleDTO) ;
    }

    /**
     * 保存进货规则
     * @param shopID
     */
    public  void saveStockRole(StockRoleDTO stockRole , String shopID){
        String stockRoleJsonStr = JSONUtil.toJsonStr(stockRole);
        ShopRole shopRole = new ShopRole();
        shopRole.setShopRoleValue(stockRoleJsonStr);
        shopRoleMapper.update(shopRole ,new QueryWrapper<ShopRole>()
                .eq("shop_role_type",STOCK_ROLE)
                .eq("shop_ID" , shopID)) ;
    }

    /**
     * 解析进货规则
     * @param shopID
     */
    public  StockRoleDTO parseStockRole(String shopID){
        ShopRole shopRole = shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                .eq("shop_role_type", STOCK_ROLE)
                .eq("shop_ID", shopID)) ;
        String stockRoleJsonStr = shopRole.getShopRoleValue() ;
        return JSONUtil.toBean(stockRoleJsonStr, StockRoleDTO.class) ;
    }

    public  String getInitStockRole(){
        StockRoleDTO stockRoleDTO = new StockRoleDTO();
        stockRoleDTO.setMinEmployee(5);
        stockRoleDTO.setMinLastTime(2);
        stockRoleDTO.setMaxLastTime(4);
        return JSONUtil.toJsonStr(stockRoleDTO) ;
    }

    /**
     * 保存客流规则
     * @param shopID
     */
    public  void saveFLowRole(FlowRoleDTO flowRole , String shopID){
        String flowRoleJsonStr = JSONUtil.toJsonStr(flowRole);
        ShopRole shopRole = new ShopRole();
        shopRole.setShopRoleValue(flowRoleJsonStr);
        shopRoleMapper.update(shopRole ,new QueryWrapper<ShopRole>()
                .eq("shop_role_type",FLOW_ROLE)
                .eq("shop_ID" , shopID)) ;
    }

    /**
     * 解析客流规则
     * @param shopID
     */
    public  FlowRoleDTO parseFlowRole(String shopID){
        ShopRole shopRole = shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                .eq("shop_role_type", FLOW_ROLE)
                .eq("shop_ID", shopID)) ;
        String flowRoleJsonStr = shopRole.getShopRoleValue() ;
        return JSONUtil.toBean(flowRoleJsonStr, FlowRoleDTO.class) ;
    }

    /**
     * 获取初始化的客流量信息
     * @return
     */
    public  String getInitFlowRole(){
        FlowRoleDTO flowRoleDTO = new FlowRoleDTO();
        flowRoleDTO.setBaseNum(3.8);
        return JSONUtil.toJsonStr(flowRoleDTO) ;
    }

    /**
     * 保存分组规则
     * @param shopID
     */
    public  void saveGroupRole(GroupRoleDTO groupRole , String shopID){
        String groupRoleJsonStr = JSONUtil.toJsonStr(groupRole);
        ShopRole shopRole = new ShopRole();
        shopRole.setShopRoleValue(groupRoleJsonStr);
        shopRoleMapper.update(shopRole ,new QueryWrapper<ShopRole>()
                .eq("shop_role_type",GROUP_ROLE)
                .eq("shop_ID" , shopID)) ;
    }

    /**
     * 解析分组规则
     * @param shopID
     */
    public  GroupRoleDTO parseGroupRole(String shopID){
        ShopRole shopRole = shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                .eq("shop_role_type", GROUP_ROLE)
                .eq("shop_ID", shopID)) ;
        String groupRoleJsonStr = shopRole.getShopRoleValue() ;
        return JSONUtil.toBean(groupRoleJsonStr, GroupRoleDTO.class) ;
    }

    /**
     * 初始化分组规则
     * @return
     */
    public  String getInitGroupRole(){
        GroupRoleDTO groupRoleDTO = new GroupRoleDTO();
        groupRoleDTO.setDeadLineEmployee(5);
        return JSONUtil.toJsonStr(groupRoleDTO) ;
    }

    public  void saveSystemLastTime(SystemLastTimeDTO systemLastTimeDTO , String shopID){
        String systemLastTimeDTOJsonStr = JSONUtil.toJsonStr(systemLastTimeDTO);
        SystemRole systemRole = new SystemRole();
        systemRole.setSystemRoleValue(systemLastTimeDTOJsonStr);
        systemRoleMapper.update(systemRole ,new QueryWrapper<SystemRole>()
                .eq("system_role_type",SYSTEM_LAST_TIME)
                .eq("shop_ID" , shopID)) ;
    }

    public  void saveSystemWorkTime(SystemWorkTimeDTO systemWorkTimeDTO , String shopID){
        String systemWorkTimeDTOJsonStr = JSONUtil.toJsonStr(systemWorkTimeDTO);
        SystemRole systemRole = new SystemRole();
        systemRole.setSystemRoleValue(systemWorkTimeDTOJsonStr);
        systemRoleMapper.update(systemRole ,new QueryWrapper<SystemRole>()
                .eq("system_role_type",SYSTEM_WORK_TIME)
                .eq("shop_ID" , shopID)) ;
    }

    public  void saveSystemRestTime(SystemRestTimeDTO systemRestTimeDTO , String shopID){
        String systemRestTimeDTOJsonStr = JSONUtil.toJsonStr(systemRestTimeDTO);
        SystemRole systemRole = new SystemRole();
        systemRole.setSystemRoleValue(systemRestTimeDTOJsonStr);
        systemRoleMapper.update(systemRole ,new QueryWrapper<SystemRole>()
                .eq("system_role_type",SYSTEM_REST_TIME)
                .eq("shop_ID" , shopID)) ;
    }

    public  void saveSystemGroupPosition(SystemGroupPositionDTO systemGroupPosition , String shopID){
        String systemGroupPositionDTOJsonStr = JSONUtil.toJsonStr(systemGroupPosition);
        SystemRole systemRole = new SystemRole();
        systemRole.setSystemRoleValue(systemGroupPositionDTOJsonStr);
        systemRoleMapper.update(systemRole ,new QueryWrapper<SystemRole>()
                .eq("system_role_type",SYSTEM_GROUP_POSITION)
                .eq("shop_ID" , shopID)) ;
    }

    public  String getInitSystemLastTimeDTO(){
        SystemLastTimeDTO systemLastTimeDTO = new SystemLastTimeDTO();
        systemLastTimeDTO.setWorkDay("1,5");
        systemLastTimeDTO.setWorkStart(9);
        systemLastTimeDTO.setWorkEnd(21);
        systemLastTimeDTO.setWeekend("6,7");
        systemLastTimeDTO.setWeekendStart(10);
        systemLastTimeDTO.setWorkEnd(22);
        return JSONUtil.toJsonStr(systemLastTimeDTO) ;
    }

    public  String getInitSystemWorkTimeDTO(){
        SystemWorkTimeDTO systemWorkTimeDTO = new SystemWorkTimeDTO();
        systemWorkTimeDTO.setWeekMaxTime(40);
        systemWorkTimeDTO.setDayMaxTime(8);
        systemWorkTimeDTO.setLocationMinTime(2);
        systemWorkTimeDTO.setLocationMaxTime(4);
        return JSONUtil.toJsonStr(systemWorkTimeDTO) ;
    }

    public  String getInitSystemRestTimeDTO(){
        SystemRestTimeDTO systemRestTimeDTO = new SystemRestTimeDTO();
        systemRestTimeDTO.setLunchTime(0.5);
        systemRestTimeDTO.setLunchTimeStart(11);
        systemRestTimeDTO.setLunchTimeEnd(14);
        systemRestTimeDTO.setDinnerTime(0.5);
        systemRestTimeDTO.setDinnerTimeStart(17);
        systemRestTimeDTO.setDinnerTimeEnd(20);
        systemRestTimeDTO.setRestTime(MIN_WORKINGTIME);
        return JSONUtil.toJsonStr(systemRestTimeDTO) ;
    }

    public  String getInitSystemGroupPositionDTO(){
        SystemGroupPositionDTO systemGroupPositionDTO = new SystemGroupPositionDTO();
        HashMap<String, Integer> map = new HashMap<>();
        map.put("前台",1) ;
        map.put("导购",1) ;
        map.put("库房",1) ;
        systemGroupPositionDTO.setPositionMap(map);
        ArrayList<String> positions = new ArrayList<>();
        positions.add("前台") ;
        positions.add("导购") ;
        positions.add("库房") ;
        systemGroupPositionDTO.setPositions(positions);
        return JSONUtil.toJsonStr(systemGroupPositionDTO) ;
    }

    public <E> E parseSystemRole(Class<E> type ,String systemRoleType ,String employeeID){
        SystemRole systemRole = systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                .eq("system_role_type", systemRoleType)
                .eq("employee_ID", employeeID));
        return JSONUtil.toBean(systemRole.getSystemRoleValue(),type);
    }

    public <E> void saveEmployeeRole(E obj , E employeeRoleDTO , String hobbyType ,String employeeID ){
        String JsonStr = JSONUtil.toJsonStr(employeeRoleDTO);
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setHobbyValue(JsonStr);
        employeeRoleMapper.update(employeeRole ,new QueryWrapper<EmployeeRole>()
                .eq("hobby_type",hobbyType)
                .eq("employee_ID" , employeeID)) ;
    }

    public <E> E parseEmployeeRole(Class<E> type , String hobbyType ,String employeeID ){
        EmployeeRole employeeRole = employeeRoleMapper.selectOne(new QueryWrapper<EmployeeRole>()
                        .eq("hobby_type", hobbyType)
                        .eq("employee_ID", employeeID));
        return JSONUtil.toBean(employeeRole.getHobbyValue() , type ) ;
    }
}
