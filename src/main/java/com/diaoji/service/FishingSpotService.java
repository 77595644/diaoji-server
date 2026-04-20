package com.diaoji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.diaoji.entity.FishingSpot;

import java.math.BigDecimal;
import java.util.List;

public interface FishingSpotService extends IService<FishingSpot> {
    Page<FishingSpot> queryNearby(BigDecimal lat, BigDecimal lng,
                                  Integer radius, Integer zoom,
                                  Integer page, Integer size);
    List<FishingSpot> searchByKeyword(String keyword);
    FishingSpot getSpotDetail(Long id);
    void addSpot(FishingSpot spot);
    void addReview(Long spotId, Long userId, Integer rating, String content);
    Page<FishingSpot> getMySpots(Long userId, Integer page, Integer size);
}
