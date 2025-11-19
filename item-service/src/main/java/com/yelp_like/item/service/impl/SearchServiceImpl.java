package com.yelp_like.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yelp_like.api.dto.ItemDTO;
import com.yelp_like.common.domain.PageDTO;
import com.yelp_like.common.domain.PageQuery;
import com.yelp_like.common.exception.BadRequestException;
import com.yelp_like.item.config.EsProperties;
import com.yelp_like.item.domain.po.Item;
import com.yelp_like.item.domain.po.ItemDoc;
import com.yelp_like.item.domain.query.ItemPageQuery;
import com.yelp_like.item.service.IItemService;
import com.yelp_like.item.service.ISearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

    private final RestHighLevelClient client;
    private final EsProperties esProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IItemService itemService;

    @Override
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
        try {
            SearchSourceBuilder source = new SearchSourceBuilder();
            BoolQueryBuilder bool = QueryBuilders.boolQuery();
            if (StrUtil.isNotBlank(query.getKey())) {
                bool.must(QueryBuilders.matchQuery("name", query.getKey()).operator(Operator.AND));
            } else {
                bool.must(QueryBuilders.matchAllQuery());
            }
            if (StrUtil.isNotBlank(query.getBrand())) {
                bool.filter(QueryBuilders.termQuery("brand.keyword", query.getBrand()));
            }
            if (StrUtil.isNotBlank(query.getCategory())) {
                bool.filter(QueryBuilders.termQuery("category.keyword", query.getCategory()));
            }
            if (query.getMinPrice() != null || query.getMaxPrice() != null) {
                bool.filter(QueryBuilders.rangeQuery("price")
                        .from(query.getMinPrice() == null ? 0 : query.getMinPrice())
                        .to(query.getMaxPrice() == null ? Integer.MAX_VALUE : query.getMaxPrice()));
            }
            bool.filter(QueryBuilders.termQuery("status", 1));
            source.query(bool);
            int pageSize = query.getPageSize() == null ? PageQuery.DEFAULT_PAGE_SIZE : query.getPageSize();
            int from = query.from();
            source.from(from);
            source.size(pageSize);
            if (StrUtil.isNotBlank(query.getSortBy())) {
                source.sort(query.getSortBy(), query.getIsAsc() == null || query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
            } else {
                source.sort("updateTime", SortOrder.DESC);
            }
            source.highlighter(new HighlightBuilder()
                    .field("name")
                    .preTags("<em>")
                    .postTags("</em>")
                    .requireFieldMatch(false));
            source.timeout(TimeValue.timeValueSeconds(3));
            SearchRequest request = new SearchRequest(esProperties.getIndex()).source(source);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            List<ItemDTO> results = new ArrayList<>();
            for (SearchHit hit : hits.getHits()) {
                ItemDoc doc = objectMapper.readValue(hit.getSourceAsString(), ItemDoc.class);
                HighlightField field = hit.getHighlightFields().get("name");
                if (field != null && field.fragments() != null && field.fragments().length > 0) {
                    doc.setName(field.fragments()[0].string());
                }
                results.add(convertDoc(doc));
            }
            long total = hits.getTotalHits() == null ? results.size() : hits.getTotalHits().value;
            return PageDTO.of(total, pageSize, results);
        } catch (IOException e) {
            log.error("搜索商品失败", e);
            throw new BadRequestException("搜索商品失败，请稍后重试");
        }
    }

    @Override
    public void syncById(Long itemId) {
        Item item = itemService.getById(itemId);
        if (item == null || item.getStatus() == null || item.getStatus() == 3) {
            deleteById(itemId);
            return;
        }
        ItemDoc doc = buildDoc(item);
        IndexRequest request = new IndexRequest(esProperties.getIndex())
                .id(String.valueOf(itemId))
                .source(writeDoc(doc), XContentType.JSON);
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("索引商品失败,id={}", itemId, e);
            throw new BadRequestException("索引商品失败");
        }
    }

    @Override
    public void deleteById(Long itemId) {
        DeleteRequest request = new DeleteRequest(esProperties.getIndex(), String.valueOf(itemId));
        try {
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("删除索引商品失败,id={}", itemId, e);
        }
    }

    private ItemDTO convertDoc(ItemDoc doc) {
        ItemDTO dto = new ItemDTO();
        dto.setId(doc.getId() == null ? null : Long.valueOf(doc.getId()));
        dto.setName(doc.getName());
        dto.setPrice(doc.getPrice());
        dto.setStock(doc.getStock());
        dto.setImage(doc.getImage());
        dto.setCategory(doc.getCategory());
        dto.setBrand(doc.getBrand());
        dto.setSpec(doc.getSpec());
        dto.setSold(doc.getSold());
        dto.setCommentCount(doc.getCommentCount());
        dto.setIsAD(doc.getIsAD());
        dto.setStatus(doc.getStatus());
        return dto;
    }

    private ItemDoc buildDoc(Item item) {
        ItemDoc doc = new ItemDoc();
        doc.setId(String.valueOf(item.getId()));
        doc.setName(item.getName());
        doc.setPrice(item.getPrice());
        doc.setStock(item.getStock());
        doc.setImage(item.getImage());
        doc.setCategory(item.getCategory());
        doc.setBrand(item.getBrand());
        doc.setSpec(item.getSpec());
        doc.setSold(item.getSold());
        doc.setCommentCount(item.getCommentCount());
        doc.setIsAD(item.getIsAD());
        doc.setStatus(item.getStatus());
        doc.setUpdateTime(item.getUpdateTime() == null ? null :
                item.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return doc;
    }

    private String writeDoc(ItemDoc doc) {
        try {
            return objectMapper.writeValueAsString(doc);
        } catch (IOException e) {
            throw new BadRequestException("序列化商品文档失败");
        }
    }
}

