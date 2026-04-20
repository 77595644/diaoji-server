package com.diaoji.vo;

public class SpeciesRankingVO {
    private Integer rank;
    private String speciesName;
    private Double totalWeight;
    private Integer totalCount;

    public SpeciesRankingVO() {}

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public String getSpeciesName() { return speciesName; }
    public void setSpeciesName(String speciesName) { this.speciesName = speciesName; }
    public Double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(Double totalWeight) { this.totalWeight = totalWeight; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
}
