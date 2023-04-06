package com.fuchuang.A33.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuchuang.A33.entity.Authentication;
import org.springframework.stereotype.Repository;

import java.awt.*;
import java.util.ArrayList;

@Repository
public interface AuthenticationMapper extends BaseMapper<Authentication> {
    ArrayList<Authentication> getAuthenticationsByPosition(String ID ,String position) ;
}
