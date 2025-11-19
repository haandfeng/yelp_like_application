package com.yelp_like.service.impl;

import com.yelp_like.domain.po.OrderLogistics;
import com.yelp_like.mapper.OrderLogisticsMapper;
import com.yelp_like.service.IOrderLogisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
public class OrderLogisticsServiceImpl extends ServiceImpl<OrderLogisticsMapper, OrderLogistics> implements IOrderLogisticsService {

}
