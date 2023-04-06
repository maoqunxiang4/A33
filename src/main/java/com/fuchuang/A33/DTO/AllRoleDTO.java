package com.fuchuang.A33.DTO;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuchuang.A33.entity.ShopRole;
import com.fuchuang.A33.entity.SystemRole;
import com.fuchuang.A33.mapper.ShopRoleMapper;
import com.fuchuang.A33.mapper.SystemRoleMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.fuchuang.A33.utils.Constants.*;

@AllArgsConstructor
@Data
//所有的规则值
public class AllRoleDTO {
    private CloseRoleDTO closeRoleDTO ;
    private FlowRoleDTO flowRoleDTO ;
    private GroupRoleDTO groupRoleDTO ;
    private OpenRoleDTO openRoleDTO ;
    private StockRoleDTO stockRoleDTO ;
    private SystemLastTimeDTO systemLastTimeDTO ;
    private SystemRestTimeDTO systemRestTimeDTO ;
    private SystemWorkTimeDTO systemWorkTimeDTO ;
    private SystemGroupPositionDTO systemGroupPositionDTO ;

    public AllRoleDTO(String shopID , ShopRoleMapper shopRoleMapper ,SystemRoleMapper systemRoleMapper){
        this.closeRoleDTO = getObject(CloseRoleDTO.class
                ,shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                                            .eq("shop_ID",shopID)
                                            .eq("shop_role_type",CLOSE_ROLE)).getShopRoleValue());
        this.flowRoleDTO = getObject(FlowRoleDTO.class
                ,shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                                            .eq("shop_ID",shopID)
                                             .eq("shop_role_type",FLOW_ROLE)).getShopRoleValue()) ;
        this.groupRoleDTO = getObject(GroupRoleDTO.class
                ,shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                                                .eq("shop_ID",shopID)
                                                .eq("shop_role_type",GROUP_ROLE)).getShopRoleValue()) ;
        this.openRoleDTO = getObject(OpenRoleDTO.class
                ,shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                                                .eq("shop_ID",shopID)
                                                .eq("shop_role_type",OPEN_ROLE)).getShopRoleValue()) ;
        this.stockRoleDTO = getObject(StockRoleDTO.class
                ,shopRoleMapper.selectOne(new QueryWrapper<ShopRole>()
                                                .eq("shop_ID",shopID)
                                                .eq("shop_role_type",STOCK_ROLE)).getShopRoleValue()) ;
        this.systemLastTimeDTO = getObject(SystemLastTimeDTO.class
                ,systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                                                .eq("shop_ID",shopID)
                                                .eq("system_role_type",SYSTEM_LAST_TIME)).getSystemRoleValue()) ;
        this.systemRestTimeDTO = getObject(SystemRestTimeDTO.class
                ,systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                                                .eq("shop_ID",shopID)
                                                .eq("system_role_type",SYSTEM_REST_TIME)).getSystemRoleValue()) ;
        this.systemWorkTimeDTO = getObject(SystemWorkTimeDTO.class
                ,systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                                                .eq("shop_ID",shopID)
                                                .eq("system_role_type",SYSTEM_WORK_TIME)).getSystemRoleValue()) ;
        this.systemGroupPositionDTO = getObject(SystemGroupPositionDTO.class
                , systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                                                .eq("shop_ID",shopID)
                                                .eq("system_role_type",SYSTEM_GROUP_POSITION)).getSystemRoleValue()) ;
    }

    public <E> E getObject(Class<E> type, String jsonStr){
        return JSONUtil.toBean(jsonStr,type) ;
    }
}
