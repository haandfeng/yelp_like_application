package com.yelp_like.trade.constants;

public interface MQConstants {
    String DELAY_EXCHANGE_NAME = "trade.delay.direct";
    String DELAY_ORDER_QUEUE_NAME = "trade.delay.order.queue";
    String DELAY_ORDER_KEY = "delay.order.query";

    String SECKILL_EXCHANGE_NAME = "trade.seckill.direct";
    String SECKILL_ORDER_QUEUE_NAME = "trade.seckill.order.queue";
    String SECKILL_ORDER_KEY = "seckill.order.create";
}
