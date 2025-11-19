package com.yelp_like.item.controller;


import com.yelp_like.api.dto.ItemDTO;
import com.yelp_like.common.domain.PageDTO;
import com.yelp_like.item.domain.query.ItemPageQuery;
import com.yelp_like.item.service.ISearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ISearchService searchService;

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
        return searchService.search(query);
    }

    @ApiOperation("同步单个商品到ES")
    @PostMapping("/index/{id}")
    public void index(@PathVariable("id") Long id) {
        searchService.syncById(id);
    }

    @ApiOperation("从ES删除商品文档")
    @DeleteMapping("/index/{id}")
    public void delete(@PathVariable("id") Long id) {
        searchService.deleteById(id);
    }
}
