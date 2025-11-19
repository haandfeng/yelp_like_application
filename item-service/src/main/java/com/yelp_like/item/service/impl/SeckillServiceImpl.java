package com.yelp_like.item.service.impl;

import com.yelp_like.api.dto.SeckillPublishDTO;
import com.yelp_like.common.constants.RedisConstants;
import com.yelp_like.common.exception.BadRequestException;
import com.yelp_like.item.domain.po.Item;
import com.yelp_like.item.service.IItemService;
import com.yelp_like.item.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements ISeckillService {

    private final IItemService itemService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void publish(SeckillPublishDTO dto) {
        Assert.notNull(dto, "DTO must not be null");
        if (dto.getItemId() == null || dto.getStock() == null || dto.getStock() <= 0) {
            throw new BadRequestException("参数不合法");
        }
        Item item = itemService.getById(dto.getItemId());
        if (item == null) {
            throw new BadRequestException("商品不存在");
        }
        stringRedisTemplate.opsForValue()
                .set(RedisConstants.SECKILL_STOCK_KEY + dto.getItemId(), dto.getStock().toString());
        stringRedisTemplate.delete(RedisConstants.SECKILL_ORDER_KEY + dto.getItemId());
    }
}

