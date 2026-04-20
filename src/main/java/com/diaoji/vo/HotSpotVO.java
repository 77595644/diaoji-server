package com.diaoji.vo;

public class HotSpotVO {
    private Integer rank;
    private Long spotId;
    private String spotName;
    private String city;
    private Integer heatScore;
    private String period;

    public HotSpotVO() {}

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Long getSpotId() { return spotId; }
    public void setSpotId(Long spotId) { this.spotId = spotId; }
    public String getSpotName() { return spotName; }
    public void setSpotName(String spotName) { this.spotName = spotName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Integer getHeatScore() { return heatScore; }
    public void setHeatScore(Integer heatScore) { this.heatScore = heatScore; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
