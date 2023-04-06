package com.fuchuang.A33.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fuchuang.A33.DTO.*;
import com.fuchuang.A33.entity.*;
import com.fuchuang.A33.mapper.*;
import com.fuchuang.A33.service.IEmployeeService;
import com.fuchuang.A33.utils.Constants;
import com.fuchuang.A33.utils.EmployeeHolder;
import com.fuchuang.A33.utils.Result;
import com.fuchuang.A33.utils.RoleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.fuchuang.A33.utils.Constants.*;

@Service
public class EmployeeServiceImpl implements IEmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper ;

    @Autowired
    private StringRedisTemplate stringRedisTemplate ;

    @Autowired
    private AuthenticationManager authenticationManager ;

    @Autowired
    private EmployeeRoleMapper employeeRoleMapper ;

    @Autowired
    private WorkingMapper workingMapper ;

    @Autowired
    private LocationMapper locationMapper ;

    @Autowired
    private ShopMapper shopMapper ;

    @Autowired
    private TimesMapper timesMapper ;

    @Autowired
    private ShopRoleMapper shopRoleMapper ;


    @Autowired
    private SystemRoleMapper systemRoleMapper ;

    @Override
    public Result login(String email) {
        Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("email", email));
        if (Objects.isNull(employee)){
            return Result.fail(500,"can not find the employee") ;
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(email,"");
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        String token = UUID.randomUUID().toString();
        //过滤器第一次无法得到的token,需要我们手动去传递
        LoginEmployee loginEmployee = (LoginEmployee) authenticate.getPrincipal();
        EmployeeDTO employeeDTO = new EmployeeDTO();
        //注意不能直接将loginEmployee赋值给employeeDTo，而是应该获取loginEmployee中的Employee部分
        BeanUtil.copyProperties(loginEmployee.getEmployee(),employeeDTO);
        employeeDTO.setPermissions(loginEmployee.getPermissions());
        Map<String, Object> map = BeanUtil.beanToMap(employeeDTO ,
                new HashMap<>() ,
                CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((filedName,filedValue)->{
                    if (filedValue == null) {
                        filedValue = "0" ;
                    }else {
                        filedValue = filedValue.toString() ;
                    }
                    return filedValue ;
                }));
        stringRedisTemplate.opsForHash().putAll(Constants.EMPLOYEE_TOKEN + token,map);
        stringRedisTemplate.expire(Constants.EMPLOYEE_TOKEN + token,60*30, TimeUnit.SECONDS) ;
        EmployeeAndTokenDTO employeeAndTokenDTO = new EmployeeAndTokenDTO();
        BeanUtil.copyProperties(employee , employeeAndTokenDTO);
        employeeAndTokenDTO.setToken(token);
        stringRedisTemplate.opsForValue().set(GET_TOKEN + employee.getID() , token);
        return Result.success(200,employeeAndTokenDTO);
    }

    @Override
    public Result regist(String name, String email, String position, String shopId ,String belong ,String phone) {
        if (name ==null || email ==null || position ==null || shopId ==null  || phone ==null ){
            return Result.fail(500,"filed can not be null") ;
        }
        ArrayList<String> list = new ArrayList<>();
        list.add("店长") ;list.add("经理") ;list.add("小组长") ;list.add("收营员") ;list.add("导购") ;list.add("库房") ;
        if (!list.contains(position)){
            return Result.fail(500,"The position is illegal") ;
        }

        Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("email", email));
        if(!Objects.isNull(employee)){
            return Result.fail(500,"this email has already registed ");
        }
        if (position.equals("root")){
            return Result.fail(500,"position can not be root") ;
        }
        long count = employeeMapper.selectCount(new QueryWrapper<>()) + 1;
        String ID ;
        if(count<10) ID = "0" + count ;
        else ID = Long.toString(count);
        //如果belong为空，表明没有小组长
        if (belong == null) {
            if(employeeMapper.insert(new Employee(ID, name, email, position, shopId , null ,phone ))==0){
                return Result.fail(500,"the system has some wrongs now") ;
            }
        }

        //如果belong不为空，查询并插入
        else {
            Employee group = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("email", belong));
            if(employeeMapper.insert(new Employee(ID, name, email, position, shopId , group.getID() , phone ))==0){
                return Result.fail(500,"the system has some wrongs now") ;
            }
        }

        //添加默认的员工规则
        //TODO 这个地方似乎有点问题
        long allCounts = timesMapper.selectCount(new QueryWrapper<>()) + 1;
        if(allCounts<10) ID = "0" + allCounts ;
        else ID = Long.toString(allCounts);
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeID(ID);
        employeeRole.setHobbyType("工作日偏好") ;
        employeeRole.setHobbyValue(DEFAULT_HOBBY_VALUE);
        employeeRoleMapper.insert(employeeRole) ;
        employeeRole.setEmployeeID(ID);
        employeeRole.setHobbyType("工作时间偏好") ;
        employeeRole.setHobbyValue(DEFAULT_HOBBY_VALUE);
        employeeRoleMapper.insert(employeeRole) ;
        employeeRole.setEmployeeID(ID);
        employeeRole.setHobbyValue("班次时长偏好") ;
        employeeRole.setHobbyValue(DEFAULT_HOBBY_VALUE);
        employeeRoleMapper.insert(employeeRole) ;

        //TODO  修改时间
        //添加times规则
        int ros = timesMapper.insert(new Times(ID, 0, 0, 40));
        if (ros==0){
            return Result.fail(500,"the system has some wrongs") ;
        }
        return Result.success(200) ;
    }

    @Override
    public Result changeGroup(String groupEmail) {
        Employee group = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("email", groupEmail));
        if (group==null){
            return Result.fail(500,"the system has some wrongs now") ;
        }
        if (group.getPosition().equals("小组长")){
            return Result.fail(500,"the email's owner is not '小组长' ") ;
        }
        EmployeeDTO employeeDTO = EmployeeHolder.getEmloyee();
        Employee em = new Employee();
        em.setID(employeeDTO.getID());
        em.setBelong(group.getID());
        employeeMapper.update(em,new QueryWrapper<Employee>().eq("ID",em.getID()));
        return Result.success(200) ;
    }

    @Override
    public Result updateEmployeeInformation(String email ,String phone) {
        Employee employee = new Employee();
        employee.setEmail(email);
        employee.setPhone(phone);
        int rows = employeeMapper.update(employee, new UpdateWrapper<Employee>().eq("ID", EmployeeHolder.getEmloyee().getID()));
        if (rows==0){
            Result.fail(500,"please try it again") ;
        }
        return Result.success(200);
    }

    @Override
    public Result showOtherImformation() {
        EmployeeDTO em = EmployeeHolder.getEmloyee();
        switch (em.getPosition()){
            case "店长" :
            case "经理" : {
                List<Employee> employeeList = employeeMapper.selectList
                        (new QueryWrapper<Employee>().eq("shop_ID", em.getShopID()));
                ArrayList<EmployeeDetailsInformationDTO> employeeDetailsInformationDTOS = new ArrayList<>();
                for (Employee employee : employeeList) {
                    EmployeeDetailsInformationDTO employeeDetailsInformationDTO = new EmployeeDetailsInformationDTO();
                    BeanUtil.copyProperties(employee, employeeDetailsInformationDTO);
                    employeeDetailsInformationDTO = setShopNameAndGroupName2EmployeeDetailsInformation(employeeDetailsInformationDTO, employee) ;
                    //查询员工偏好信息
                    //根据偏好类型依次赋值到员工具体信息中
                    employeeDetailsInformationDTOS = Employee2EmployeeDetailsInformationDTO(
                            employeeDetailsInformationDTOS ,
                            employeeDetailsInformationDTO,
                            employee) ;
                }

                return Result.success(200, employeeDetailsInformationDTOS);
            }
            case "小组长" :{
                //得到所有的店员
                List<Employee> employees = employeeMapper.selectList(new QueryWrapper<Employee>().eq(("shop_ID"), em.getShopID()));
                //对得到的信息进行过滤，过滤掉该小组的组员
                employees.removeIf(employee -> employee.getBelong().equals(em.getID()));
                ArrayList<EmployeeInformationDTO> employeeInformationDTOS = new ArrayList<>();
                for (Employee employee : employees) {
                    EmployeeInformationDTO employeeInformationDTO = new EmployeeInformationDTO();
                    BeanUtil.copyProperties(employee,employeeInformationDTO);
                    employeeInformationDTO =
                            setShopNameAndGroupName2EmployeeInformation(employeeInformationDTO, employee);
                    employeeInformationDTOS.add(employeeInformationDTO) ;
                }
                ArrayList<EmployeeDetailsInformationDTO> employeeDetailsInformationDTOS = new ArrayList<>();
                //对组内人员的信息进行详细化处理
                List<Employee> groupEmployee = employeeMapper.selectList(new QueryWrapper<Employee>().eq(("belong"), em.getID()));
                for (Employee employee : groupEmployee) {
                    EmployeeDetailsInformationDTO employeeDetailsInformationDTO = new EmployeeDetailsInformationDTO();
                    BeanUtil.copyProperties(employee,employeeDetailsInformationDTO);
                    employeeDetailsInformationDTO = setShopNameAndGroupName2EmployeeDetailsInformation(employeeDetailsInformationDTO,employee) ;
                    employeeDetailsInformationDTOS = Employee2EmployeeDetailsInformationDTO(
                            employeeDetailsInformationDTOS, employeeDetailsInformationDTO, employee);
                }
                ArrayList<Object> objects = new ArrayList<>();
                objects.addAll(employeeInformationDTOS) ;
                objects.addAll(employeeDetailsInformationDTOS) ;
                return Result.success(200,objects) ;
            }
            case "店员" :{
                List<Employee> employeeList = employeeMapper.selectList
                        (new QueryWrapper<Employee>().eq("shop_ID", em.getShopID()));
                ArrayList<EmployeeInformationDTO> employeeDTOS = new ArrayList<>();
                for (Employee employee : employeeList) {
                    EmployeeInformationDTO employeeInformationDTO = new EmployeeInformationDTO();
                    BeanUtil.copyProperties(employee,employeeInformationDTO);
                    employeeDTOS = Employee2EmployeeInformationDTO(employeeDTOS,employeeInformationDTO,employee) ;
                }
                return Result.success(200,employeeDTOS) ;
            }
        }
        return Result.success(200) ;
    }

    @Override
    public Result showEmployeeByRoot(String shopID) {
        //查询店铺内所有员工的信息
        List<Employee> employeeList = employeeMapper.selectList(new QueryWrapper<Employee>().eq("shop_ID", shopID));
        ArrayList<EmployeeDetailsInformationDTO> employeeDetailsInformationDTOS = new ArrayList<>();
        for (Employee employee : employeeList) {
            EmployeeDetailsInformationDTO employeeDetailsInformationDTO = new EmployeeDetailsInformationDTO();
            BeanUtil.copyProperties(employee,employeeDetailsInformationDTO);
            employeeDetailsInformationDTO = setShopNameAndGroupName2EmployeeDetailsInformation(employeeDetailsInformationDTO, employee) ;
            employeeDetailsInformationDTOS = Employee2EmployeeDetailsInformationDTO( employeeDetailsInformationDTOS ,
                    employeeDetailsInformationDTO, employee) ;

        }
        return Result.success(200, employeeDetailsInformationDTOS);
    }

    @Override
    public Result updateOtherImformation(String ID, String name , String email, String position, String belong ,String phone) {
        Employee group = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("email",belong)) ;
        if (!Objects.isNull(group))  belong = group.getID() ;
        else belong = "00" ;

        boolean isHighAuthentication = position.equals("小组长") || position.equals("店长") || position.equals("经理");
        if (!belong.equals("00") && isHighAuthentication){
            belong = "00" ;
        }
        if (belong.equals("00") && !isHighAuthentication){
            return Result.fail(500,"the group must be changed ") ;
        }

        Employee isRoot = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", ID));
        if (Objects.isNull(isRoot)){
            return Result.fail(500,"the employee can not be found") ;
        }
        if (isRoot.getPosition().equals("root")||isRoot.getPosition().equals("店长")){
            return Result.fail(500,"the root can not be found") ;
        }

        //限定只能修改root和boss权限以下和本商店的员工
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setPosition(position);
        employee.setBelong(belong);
        employee.setPhone(phone);
        int rows = employeeMapper.update(employee,
                new UpdateWrapper<Employee>()
                        .eq("shop_ID",isRoot.getShopID())
                        .eq("ID", ID));
        if (rows==0){
            return Result.fail(500,"please try it again") ;
        }
        return Result.success(200);
    }

    @Override
    public Result showAllShop() {
        List<Shop> shops = shopMapper.selectList(new QueryWrapper<>());
        return Result.success(200,shops);
    }

    @Override
    public Result removeEmployee(String ID) {
        employeeMapper.delete(new QueryWrapper<Employee>().eq("ID",ID)) ;
        timesMapper.delete(new QueryWrapper<Times>().eq("employee_ID",ID)) ;
        List<Working> workingList = workingMapper.selectList(new QueryWrapper<Working>().eq("employee_ID", ID));
        for (Working working : workingList) {
            locationMapper.delete(new QueryWrapper<Location>().eq("ID",working.getLocationID())) ;
        }
        workingMapper.deleteById(new QueryWrapper<Working>().eq("employee_ID",ID)) ;
        return null;
    }


    public ArrayList<EmployeeInformationDTO> Employee2EmployeeInformationDTO(ArrayList<EmployeeInformationDTO> employeeInformationDTOS ,
                                                                                    EmployeeInformationDTO employeeInformationDTO,
                                                                                    Employee employee){
        Employee group = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", employee.getBelong()));
        if (!Objects.isNull(group))employeeInformationDTO.setGroupName(group.getName());
        else employeeInformationDTO.setGroupName("无");
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("ID", employee.getShopID()));
        employeeInformationDTO.setShopName(shop.getName());
        employeeInformationDTOS.add(employeeInformationDTO) ;
        return employeeInformationDTOS;
    }

    public ArrayList<EmployeeDetailsInformationDTO> Employee2EmployeeDetailsInformationDTO(ArrayList<EmployeeDetailsInformationDTO> employeeDetailsInformationDTOS ,
                    EmployeeDetailsInformationDTO employeeDetailsInformationDTO,
                    Employee employee){
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);
        //查询员工偏好信息
        List<EmployeeRole> employeeRoles = employeeRoleMapper.selectList
                (new QueryWrapper<EmployeeRole>().eq("employee_ID", employee.getID()));
        //根据偏好类型依次赋值到员工具体信息中
        setAllEmployeeRoleValue(employeeRoles , employeeDetailsInformationDTO , shopRoleMapper ,
                systemRoleMapper , employeeRoleMapper);
//        for (EmployeeRole employeeRole : employeeRoles) {
//            switch (employeeRole.getHobbyType()){
//                case Constants.EMPLOYEEROLE_TYPE1 : {
//                    if (employeeRole.getHobbyValue() == null) employeeDetailsInformationDTO.setEmployeeWorkDayDTO(null);
//                    else employeeDetailsInformationDTO
//                            .setEmployeeWorkDayDTO(roleUtils.parseEmployeeRole(EmployeeWorkDayDTO.class ,EMPLOYEEROLE_TYPE1
//                                    , employee.getID()));
//                    break ;
//                }
//                case Constants.EMPLOYEEROLE_TYPE2 : {
//                    if (employeeRole.getHobbyValue() == null) employeeDetailsInformationDTO.setEmployeeWorkTimeDTO(null);
//                    else employeeDetailsInformationDTO
//                            .setEmployeeWorkTimeDTO(roleUtils.parseEmployeeRole(EmployeeWorkTimeDTO.class ,EMPLOYEEROLE_TYPE2
//                                    , employee.getID()));
//                    break;
//                }
//                case Constants.EMPLOYEEROLE_TYPE3 : {
//                    if (employeeRole.getHobbyValue() == null) employeeDetailsInformationDTO.setEmployeeLastTimeDTO(null);
//                    else employeeDetailsInformationDTO
//                            .setEmployeeLastTimeDTO(roleUtils.parseEmployeeRole(EmployeeLastTimeDTO.class ,EMPLOYEEROLE_TYPE3
//                                    , employee.getID()));
//                    break;
//                }
//            }
//        }
        employeeDetailsInformationDTOS.add(employeeDetailsInformationDTO) ;
        return employeeDetailsInformationDTOS ;
    }

    public EmployeeDetailsInformationDTO setShopNameAndGroupName2EmployeeDetailsInformation(EmployeeDetailsInformationDTO employeeDetailsInformationDTO,
                                                                   Employee employee){
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("ID", employee.getShopID()));
        employeeDetailsInformationDTO.setShopName(shop.getName());
        Employee group = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", employee.getBelong()));
        if (!Objects.isNull(group)) employeeDetailsInformationDTO.setGroupName(group.getName());
        else employeeDetailsInformationDTO.setGroupName("无");
        return employeeDetailsInformationDTO ;
    }


    public EmployeeInformationDTO setShopNameAndGroupName2EmployeeInformation(EmployeeInformationDTO employeeInformationDTO, Employee employee) {
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("ID", employee.getShopID()));
        employeeInformationDTO.setShopName(shop.getName());
        Employee group = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("ID", employee.getBelong()));
        if (!Objects.isNull(group)) employeeInformationDTO.setGroupName(group.getName());
        else employeeInformationDTO.setGroupName("无");
        return employeeInformationDTO ;
    }

    public void setAllEmployeeRoleValue(List<EmployeeRole> employeeRoles , EmployeeDetailsInformationDTO employeeDetailsInformationDTO ,
                   ShopRoleMapper shopRoleMapper ,SystemRoleMapper systemRoleMapper , EmployeeRoleMapper employeeRoleMapper){
        EmployeeDTO employee = EmployeeHolder.getEmloyee();
        RoleUtils roleUtils = new RoleUtils(shopRoleMapper, systemRoleMapper, employeeRoleMapper);

        for (EmployeeRole employeeRole : employeeRoles) {
            switch (employeeRole.getHobbyType()){
                case Constants.EMPLOYEEROLE_TYPE1 : {
                    if (employeeRole.getHobbyValue() == null) employeeDetailsInformationDTO.setEmployeeWorkDayDTO(null);
                    else employeeDetailsInformationDTO
                            .setEmployeeWorkDayDTO(roleUtils.parseEmployeeRole(EmployeeWorkDayDTO.class ,EMPLOYEEROLE_TYPE1
                                    , employee.getID()));
                    break ;
                }
                case Constants.EMPLOYEEROLE_TYPE2 : {
                    if (employeeRole.getHobbyValue() == null) employeeDetailsInformationDTO.setEmployeeWorkTimeDTO(null);
                    else employeeDetailsInformationDTO
                            .setEmployeeWorkTimeDTO(roleUtils.parseEmployeeRole(EmployeeWorkTimeDTO.class ,EMPLOYEEROLE_TYPE2
                                    , employee.getID()));
                    break;
                }
                case Constants.EMPLOYEEROLE_TYPE3 : {
                    if (employeeRole.getHobbyValue() == null) employeeDetailsInformationDTO.setEmployeeLastTimeDTO(null);
                    else employeeDetailsInformationDTO
                            .setEmployeeLastTimeDTO(roleUtils.parseEmployeeRole(EmployeeLastTimeDTO.class ,EMPLOYEEROLE_TYPE3
                                    , employee.getID()));
                    break;
                }
            }
        }
    }
}
