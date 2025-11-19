package com.yelp_like.item.controller;

import com.yelp_like.api.dto.SeckillPublishDTO;
import com.yelp_like.item.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final ISeckillService seckillService;

    @PostMapping("/publish")
    public void publish(@RequestBody SeckillPublishDTO dto) {
        seckillService.publish(dto);
    }
}

