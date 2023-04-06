对于该项目来说，我们需要知道以下注意事项

1.该项目用的工具类框架有swagger和security。
swagger即接口文档，访问地址为： 域名:8001/A33/swagger-ui/index.html。
如果有不懂的操作可以去查看EmployeeController中的相关案例即可以模仿使用

security框架已基本搭建完成，如果需要添加白名单，可以去SecurityConfig中的config方法中添加白名单。如果某些方法需要响应的权限才能
访问时，使用@PreAuthorize("hasAuthority('xxx'')")既可以设置权限访问

2.使用Employee.getEmployee()即可在使用途中获得Employee实体类对象