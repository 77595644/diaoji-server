package com.diaoji.service.impl;

import com.diaoji.mapper.HomeMapper;
import com.diaoji.service.HomeService;
import com.diaoji.vo.HotSpotVO;
import com.diaoji.vo.NearbySpotVO;
import com.diaoji.vo.SpeciesRankingVO;
import com.diaoji.vo.UserRankingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private HomeMapper homeMapper;

    private static final int MAX_RANKING_TOTAL = 100;

    @Override
    public List<HotSpotVO> getSpotHeatRanking(String period) {
        List<HotSpotVO> list;
        if ("current_month".equals(period)) {
            list = homeMapper.listSpotHeatCurrentMonth();
        } else {
            // 默认近30天
            period = "month30";
            list = homeMapper.listSpotHeatMonth30();
        }
        // 补充排名 + period 字段
        int rank = 1;
        for (HotSpotVO vo : list) {
            vo.setRank(rank++);
            vo.setPeriod(period);
        }
        return list;
    }

    @Override
    public Map<String, Object> getUserRanking(String scope, String regionCode,
                                               String dimension, int page, int pageSize) {
        // 目前 Mapper 暂不支持 scope/regionCode 过滤，全量查询后内存中处理
        List<UserRankingVO> all;
        switch (dimension == null ? "weight" : dimension) {
            case "count"  -> all = homeMapper.listUserRankingByCount();
            case "trips"  -> all = homeMapper.listUserRankingByTrips();
            default       -> all = homeMapper.listUserRankingByWeight();
        }

        // 过滤前100
        if (all.size() > MAX_RANKING_TOTAL) {
            all = all.subList(0, MAX_RANKING_TOTAL);
        }

        // 补充排名 + highlight（前3名高亮）
        for (int i = 0; i < all.size(); i++) {
            UserRankingVO vo = all.get(i);
            vo.setRank(i + 1);
            vo.setHighlight(i < 3);
        }

        // 分页
        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<UserRankingVO> paged = fromIndex < total
                ? all.subList(fromIndex, toIndex)
                : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", paged);
        return result;
    }

    @Override
    public List<SpeciesRankingVO> getSpeciesRanking(int limit) {
        if (limit <= 0) limit = 10;
        List<SpeciesRankingVO> list = homeMapper.listSpeciesRanking(limit);
        int rank = 1;
        for (SpeciesRankingVO vo : list) {
            vo.setRank(rank++);
        }
        return list;
    }

    @Override
    public Map<String, Object> getNearbySpots(Double lat, Double lng, Double radius,
                                               String sortBy, int page, int pageSize) {
        if (lat == null || lng == null) {
            throw new IllegalArgumentException("lat and lng are required");
        }
        if (radius == null || radius <= 0) radius = 50.0;
        if (sortBy == null) sortBy = "distance";

        int offset = (page - 1) * pageSize;
        List<NearbySpotVO> list;
        if ("heat".equals(sortBy)) {
            list = homeMapper.listNearbyByHeat(lat, lng, radius, offset, pageSize);
        } else {
            list = homeMapper.listNearbyByDistance(lat, lng, radius, offset, pageSize);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return result;
    }
}
