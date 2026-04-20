package com.diaoji.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 手机号登录
    private String phone;
    private String password;
    
    // 密保问题
    private String securityQuestion;
    private String securityAnswer;
    
    // 用户资料
    private String nickname;
    private String avatarUrl;
    private String fishingStyles;
    private Integer guideStatus;
    
    // 微信绑定（可选）
    private String openid;
    private String unionid;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public User() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
    
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getFishingStyles() { return fishingStyles; }
    public void setFishingStyles(String fishingStyles) { this.fishingStyles = fishingStyles; }
    
    public Integer getGuideStatus() { return guideStatus; }
    public void setGuideStatus(Integer guideStatus) { this.guideStatus = guideStatus; }
    
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
