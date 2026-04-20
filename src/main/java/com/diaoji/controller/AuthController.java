package com.diaoji.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.diaoji.vo.Result;
import com.diaoji.entity.User;
import com.diaoji.mapper.UserMapper;
import com.diaoji.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    // 预设的密保问题列表
    private static final String[] SECURITY_QUESTIONS = {
        "您的出生城市是？",
        "您母亲的名字是？",
        "您第一只宠物的名字是？",
        "您小学的名字是？",
        "您最喜欢的钓鱼地点是？"
    };

    /**
     * 获取密保问题列表
     */
    @GetMapping("/questions")
    public Result getSecurityQuestions() {
        return Result.success(SECURITY_QUESTIONS);
    }

    /**
     * 获取某用户的密保问题（用于找回密码）
     */
    @GetMapping("/security-question")
    public Result getSecurityQuestion(@RequestParam String phone) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            return Result.error("该手机号未注册");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("question", user.getSecurityQuestion());
        data.put("phone", phone);
        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        String nickname = params.get("nickname");
        String securityQuestion = params.get("securityQuestion");
        String securityAnswer = params.get("securityAnswer");

        // 参数校验
        if (phone == null || phone.length() != 11) {
            return Result.error("请输入正确的手机号");
        }
        if (password == null || password.length() < 6) {
            return Result.error("密码长度至少6位");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            return Result.error("请输入昵称");
        }
        if (securityQuestion == null || securityAnswer == null) {
            return Result.error("请设置密保问题");
        }

        // 检查手机号是否已注册
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("该手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setPassword(password); // 生产环境应该加密
        user.setNickname(nickname.trim());
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswer(securityAnswer); // 生产环境应该加密
        user.setGuideStatus(0);
        user.setDeleted(0);

        userMapper.insert(user);

        // 生成 token
        String token = JwtUtil.createToken(user.getId(), phone);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        return Result.success(data);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");

        // 参数校验
        if (phone == null || phone.length() != 11) {
            return Result.error("请输入正确的手机号");
        }
        if (password == null) {
            return Result.error("请输入密码");
        }

        // 查找用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error("该手机号未注册");
        }

        // 验证密码
        if (!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        // 生成 token
        String token = JwtUtil.createToken(user.getId(), phone);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatarUrl());
        return Result.success(data);
    }

    /**
     * 验证密保答案
     */
    @PostMapping("/verify-security")
    public Result verifySecurityAnswer(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String securityAnswer = params.get("securityAnswer");

        // 查找用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error("该手机号未注册");
        }

        // 验证密保答案
        if (!securityAnswer.equals(user.getSecurityAnswer())) {
            return Result.error("密保答案错误");
        }

        // 生成临时重置 token（有效期10分钟）
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        
        Map<String, Object> data = new HashMap<>();
        data.put("resetToken", resetToken);
        data.put("message", "验证成功，请设置新密码");
        return Result.success(data);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String newPassword = params.get("newPassword");

        // 参数校验
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("密码长度至少6位");
        }

        // 查找用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error("该手机号未注册");
        }

        // 更新密码
        user.setPassword(newPassword);
        userMapper.updateById(user);

        // 生成新 token
        String token = JwtUtil.createToken(user.getId(), phone);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("message", "密码重置成功");
        return Result.success(data);
    }

    /**
     * 演示登录（开发测试用）
     */
    @PostMapping("/demo-login")
    public Result demoLogin() {
        // 检查是否存在演示用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", "13800000000");
        User demoUser = userMapper.selectOne(wrapper);

        if (demoUser == null) {
            // 创建演示用户
            demoUser = new User();
            demoUser.setPhone("13800000000");
            demoUser.setPassword("123456");
            demoUser.setNickname("演示用户");
            demoUser.setSecurityQuestion("您最喜欢的钓鱼地点是？");
            demoUser.setSecurityAnswer("珠江");
            demoUser.setGuideStatus(1);
            demoUser.setDeleted(0);
            userMapper.insert(demoUser);
        }

        // 生成 token
        String token = JwtUtil.createToken(demoUser.getId(), demoUser.getPhone());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", demoUser.getId());
        data.put("nickname", demoUser.getNickname());
        return Result.success(data);
    }
}
