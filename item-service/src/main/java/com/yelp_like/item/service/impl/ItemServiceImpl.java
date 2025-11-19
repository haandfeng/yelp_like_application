package com.yelp_like.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yelp_like.api.dto.ItemDTO;
import com.yelp_like.api.dto.OrderDetailDTO;
import com.yelp_like.common.exception.BizIllegalException;
import com.yelp_like.common.utils.BeanUtils;
import com.yelp_like.item.domain.po.Item;
import com.yelp_like.item.mapper.ItemMapper;
import com.yelp_like.item.service.IItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        String sqlStatement = "mapper.com.yelp_like.item.ItemMapper.updateStock";
        boolean r;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            throw new BizIllegalException("更新库存异常，可能是库存不足", e);
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    @Transactional
    public void recoverStock(List<OrderDetailDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        String sqlStatement = "mapper.com.yelp_like.item.ItemMapper.increaseStock";
        executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
    }
}
