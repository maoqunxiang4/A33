package com.fuchuang.A33.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@TableName("t_shop")
public class Shop {
    private String ID ;
    private String name ;
    private String address ;
    private double size ;
}
