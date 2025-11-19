package com.yelp_like.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yelp_like.pay.domain.dto.PayApplyDTO;
import com.yelp_like.pay.domain.dto.PayOrderFormDTO;
import com.yelp_like.pay.domain.po.PayOrder;

/**
 * <p>
 * 支付订单 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-16
 */
public interface IPayOrderService extends IService<PayOrder> {

    String applyPayOrder(PayApplyDTO applyDTO);

    void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO);
}
