package com.yelp_like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yelp_like.common.exception.BizIllegalException;
import com.yelp_like.common.utils.BeanUtils;
import com.yelp_like.domain.dto.ItemDTO;
import com.yelp_like.domain.dto.OrderDetailDTO;
import com.yelp_like.domain.po.Item;
import com.yelp_like.mapper.ItemMapper;
import com.yelp_like.service.IItemService;
import org.springframework.stereotype.Service;

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
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.yelp_like.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            log.error("更新库存异常", e);
            return;
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }
}
