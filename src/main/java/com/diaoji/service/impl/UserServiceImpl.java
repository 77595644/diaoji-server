package com.diaoji.service.impl;

import com.diaoji.util.JwtUtil;
import com.diaoji.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diaoji.entity.User;
import com.diaoji.mapper.UserMapper;
import com.diaoji.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${wechat.appid:YOUR_APPID}")
    private String appid;

    @Value("${wechat.secret:YOUR_SECRET}")
    private String secret;

    @Override
    public String wechatLogin(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + appid
                + "&secret=" + secret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        String response = cn.hutool.http.HttpUtil.get(url);
        cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(response);

        if (json.containsKey("errcode")) {
            throw new RuntimeException("微信登录失败：" + json.getStr("errmsg"));
        }

        String openid = json.getStr("openid");
        String unionid = json.getStr("unionid");

        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(User::getOpenid, openid);
        User user = baseMapper.selectOne(q);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            int len = Math.min(6, openid.length());
            user.setNickname("钓友" + openid.substring(0, len));
            user.setGuideStatus(0);
            baseMapper.insert(user);
            System.out.println("🆕 新用户注册：openid=" + openid);
        } else {
            System.out.println("🔑 老用户登录：openid=" + openid);
        }

        // 微信登录已改为手机号登录，此方法暂不使用
        // 保留接口兼容性，返回 JWT token
        String token = JwtUtil.createToken(user.getId(), user.getPhone() != null ? user.getPhone() : openid);
        return token;
    }

    @Autowired
    private com.diaoji.service.FishRecordService fishRecordService;

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        return fishRecordService.getStats(userId);
    }

    @Override
    public void doneGuide(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setGuideStatus(1);
        baseMapper.updateById(user);
    }
}
