package com.yelp_like.item.service;

import com.yelp_like.api.dto.ItemDTO;
import com.yelp_like.common.domain.PageDTO;
import com.yelp_like.item.domain.query.ItemPageQuery;

public interface ISearchService {

    PageDTO<ItemDTO> search(ItemPageQuery query);

    void syncById(Long itemId);

    void deleteById(Long itemId);
}

