package com.diaoji.controller;

import com.diaoji.util.UserContext;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.diaoji.entity.User;
import com.diaoji.service.UserService;
import com.diaoji.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserController(UserService userService, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "微信OAuth登录")
    @GetMapping("/login")
    public Result<String> login(@RequestParam String code) {
        String token = userService.wechatLogin(code);
        return Result.success(token);
    }

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public void captcha(HttpServletResponse response) throws IOException {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 50);
        String code = captcha.getCode();
        String captchaId = IdUtil.simpleUUID();
        redisTemplate.opsForValue().set("captcha:" + captchaId, code, 5, TimeUnit.MINUTES);
        response.setContentType("image/png");
        response.setHeader("Captcha-Id", captchaId);
        captcha.write(response.getOutputStream());
    }

    @Operation(summary = "验证图形验证码")
    @PostMapping("/verify-captcha")
    public Result<Boolean> verifyCaptcha(
            @RequestHeader("Captcha-Id") String captchaId,
            @RequestParam String code) {
        String cached = (String) redisTemplate.opsForValue().get("captcha:" + captchaId);
        if (cached != null && cached.equalsIgnoreCase(code)) {
            redisTemplate.delete("captcha:" + captchaId);
            return Result.success(true);
        }
        return Result.success(false);
    }

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public Result<User> getProfile() {
        User user = userService.getById(UserContext.getUserId());
        return Result.success(user);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User user) {
        user.setId(UserContext.getUserId());
        userService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "完成新手引导")
    @PostMapping("/guide-done")
    public Result<Void> guideDone() {
        userService.doneGuide(UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "用户数据看板")
    @GetMapping("/stats")
    public Result<Object> getStats() {
        Long userId = UserContext.getUserId();
        return Result.success(userService.getUserStats(userId));
    }

    @Operation(summary = "查看他人资料（公开，只读）")
    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        // 脱敏：隐藏密码字段
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "查看他人数据看板（公开，只读）")
    @GetMapping("/{userId}/stats")
    public Result<Object> getUserStats(@PathVariable Long userId) {
        return Result.success(userService.getUserStats(userId));
    }
}
