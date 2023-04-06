package com.fuchuang.A33.utils;

import cn.hutool.json.JSONUtil;

public class Constants {
    public static final String EMPLOYEE_TOKEN = "token:" ;
    public static final String GET_TOKEN = "UserToken:" ;
    public static final Integer EMPLOYEEROLE_NUMBER = 3 ;
    public static final String EMPLOYEEROLE_TYPE1 = "工作日偏好" ;
    public static final String EMPLOYEEROLE_TYPE2 = "工作时间偏好" ;
    public static final String EMPLOYEEROLE_TYPE3 = "班次时长偏好" ;
    //TODO 修改单次最短工作时长
    public static final double MIN_WORKINGTIME = 0.5 ;
    public static final Integer ONEDAY_COUNTS = 26 ;
    public static final String OPEN_ROLE = "开店规则" ;
    public static final String CLOSE_ROLE = "关店规则" ;
    public static final String FLOW_ROLE = "客流规则" ;
    public static final String GROUP_ROLE = "分组规则" ;
    public static final String STOCK_ROLE = "进货规则" ;
    public static final Integer ALL_LOCATIONS = 26 ;
    //TODO 写一个方法让能够自己调节时间
    public static final double MIN_LAST_TIME = 4*0.5 ;
    public static final double MIN_FLOW_TIME = 0.5 ;
    public static final String SYSTEM_LAST_TIME = "门店营业时间段" ;
    public static final String SYSTEM_WORK_TIME = "员工工作时长" ;
    public static final String SYSTEM_REST_TIME = "休息时间段" ;
    public static final String SYSTEM_GROUP_POSITION = "单次职位安排" ;
    public static final Integer FLOW_START = 8 ;
    public static final Integer MIN_PRIORITY = 3 ;
    public static final String DEFAULT_HOBBY_VALUE = "{\"value\":\"无\"}" ;
    public static final double CLEANTIME_BEFOREOPEN = 1 ;
    public static final double CLEANTIME_BEFORECLOSE = 2 ;
}
