package com.yelp_like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yelp_like.domain.dto.LoginFormDTO;
import com.yelp_like.domain.po.User;
import com.yelp_like.domain.vo.UserLoginVO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);
}
