package com.diaoji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.diaoji.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    String wechatLogin(String code);
    Map<String, Object> getUserStats(Long userId);
    void doneGuide(Long userId);
}
