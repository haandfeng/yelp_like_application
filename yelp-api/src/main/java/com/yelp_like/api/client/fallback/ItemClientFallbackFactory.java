package com.yelp_like.api.client.fallback;

import com.yelp_like.api.client.ItemClient;
import com.yelp_like.api.dto.ItemDTO;
import com.yelp_like.api.dto.OrderDetailDTO;
import com.yelp_like.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败！", cause);
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品库存失败！", cause);
                throw new RuntimeException(cause);
            }

            @Override
            public void recoverStock(List<OrderDetailDTO> items) {
                log.error("回补商品库存失败！", cause);
                throw new RuntimeException(cause);
            }
        };
    }
}
