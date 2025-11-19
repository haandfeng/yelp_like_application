package com.yelp_like.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yelp_like.api.dto.SeckillOrderMessage;
import com.yelp_like.trade.domain.dto.OrderFormDTO;
import com.yelp_like.trade.domain.po.Order;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    void cancelOrder(Long orderId);

    void createSeckillOrder(SeckillOrderMessage message);
}
