package com.diaoji.vo;

public class NearbySpotVO {
    private Long spotId;
    private String spotName;
    private String city;
    private Double distanceKm;
    private Integer heatScore;
    private Double avgRating;

    public NearbySpotVO() {}

    public Long getSpotId() { return spotId; }
    public void setSpotId(Long spotId) { this.spotId = spotId; }
    public String getSpotName() { return spotName; }
    public void setSpotName(String spotName) { this.spotName = spotName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Integer getHeatScore() { return heatScore; }
    public void setHeatScore(Integer heatScore) { this.heatScore = heatScore; }
    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
}
