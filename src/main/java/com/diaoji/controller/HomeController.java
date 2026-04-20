package com.diaoji.controller;

import com.diaoji.service.HomeService;
import com.diaoji.vo.HotSpotVO;
import com.diaoji.vo.NearbySpotVO;
import com.diaoji.vo.SpeciesRankingVO;
import com.diaoji.vo.UserRankingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /** 钓点热度榜 */
    @GetMapping("/spot-heat")
    public Map<String, Object> getSpotHeat(
            @RequestParam(value = "period", defaultValue = "month30") String period) {
        List<HotSpotVO> list = homeService.getSpotHeatRanking(period);
        return Map.of("code", 0, "data", list);
    }

    /** 用户排行榜 */
    @GetMapping("/user-ranking")
    public Map<String, Object> getUserRanking(
            @RequestParam(value = "scope", defaultValue = "country") String scope,
            @RequestParam(value = "regionCode", required = false) String regionCode,
            @RequestParam(value = "dimension", defaultValue = "weight") String dimension,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Map<String, Object> data = homeService.getUserRanking(scope, regionCode, dimension, page, pageSize);
        return Map.of("code", 0, "data", data);
    }

    /** 鱼种排行 */
    @GetMapping("/species-ranking")
    public Map<String, Object> getSpeciesRanking(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<SpeciesRankingVO> list = homeService.getSpeciesRanking(limit);
        return Map.of("code", 0, "data", list);
    }

    /** 附近钓点 */
    @GetMapping("/nearby-spots")
    public Map<String, Object> getNearbySpots(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "radius", defaultValue = "50") Double radius,
            @RequestParam(value = "sortBy", defaultValue = "distance") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Map<String, Object> data = homeService.getNearbySpots(lat, lng, radius, sortBy, page, pageSize);
        return Map.of("code", 0, "data", data);
    }
}
