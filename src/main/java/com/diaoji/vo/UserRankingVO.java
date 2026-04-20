package com.diaoji.vo;

public class UserRankingVO {
    private Integer rank;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Double value;
    private String dimension;
    private Boolean highlight;

    public UserRankingVO() {}

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public Boolean getHighlight() { return highlight; }
    public void setHighlight(Boolean highlight) { this.highlight = highlight; }
}
