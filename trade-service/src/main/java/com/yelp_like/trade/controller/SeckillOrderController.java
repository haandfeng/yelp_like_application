package com.yelp_like.trade.controller;

import com.yelp_like.trade.service.SeckillOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders/seckill")
@RequiredArgsConstructor
public class SeckillOrderController {

    private final SeckillOrderService seckillOrderService;

    @PostMapping("/{itemId}")
    public Long seckill(@PathVariable("itemId") Long itemId) {
        return seckillOrderService.seckill(itemId);
    }
}

