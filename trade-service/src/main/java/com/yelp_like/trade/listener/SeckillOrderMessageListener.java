package com.yelp_like.trade.listener;

import com.yelp_like.api.dto.SeckillOrderMessage;
import com.yelp_like.trade.constants.MQConstants;
import com.yelp_like.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderMessageListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.SECKILL_ORDER_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MQConstants.SECKILL_EXCHANGE_NAME, type = "direct"),
            key = MQConstants.SECKILL_ORDER_KEY
    ))
    public void listen(SeckillOrderMessage message) {
        try {
            orderService.createSeckillOrder(message);
        } catch (Exception e) {
            log.error("处理秒杀订单失败，message={}", message, e);
            throw e;
        }
    }
}

