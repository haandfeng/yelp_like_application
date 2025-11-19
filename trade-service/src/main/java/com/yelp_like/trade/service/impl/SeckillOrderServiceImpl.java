package com.yelp_like.trade.service.impl;

import com.yelp_like.api.dto.SeckillOrderMessage;
import com.yelp_like.common.exception.BadRequestException;
import com.yelp_like.common.redis.RedisIdWorker;
import com.yelp_like.common.utils.UserContext;
import com.yelp_like.trade.constants.MQConstants;
import com.yelp_like.trade.service.SeckillOrderService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisIdWorker redisIdWorker;
    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT = new DefaultRedisScript<>();
    static {
        SECKILL_SCRIPT.setLocation(new ClassPathResource("scripts/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Long seckill(Long itemId) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("未登录");
        }
        RLock lock = redissonClient.getLock("lock:seckill:" + userId);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new BadRequestException("请勿重复下单");
            }
            long orderId = redisIdWorker.nextId("order");
            Long result = stringRedisTemplate.execute(
                    SECKILL_SCRIPT,
                    Collections.emptyList(),
                    String.valueOf(itemId),
                    userId.toString(),
                    String.valueOf(orderId)
            );
            if (result == null) {
                throw new BadRequestException("系统繁忙");
            }
            if (result == 1L) {
                throw new BadRequestException("库存不足");
            }
            if (result == 2L) {
                throw new BadRequestException("不能重复下单");
            }
            SeckillOrderMessage message = new SeckillOrderMessage(orderId, userId, itemId, 1);
            rabbitTemplate.convertAndSend(MQConstants.SECKILL_EXCHANGE_NAME, MQConstants.SECKILL_ORDER_KEY, message);
            return orderId;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("请求超时");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}

