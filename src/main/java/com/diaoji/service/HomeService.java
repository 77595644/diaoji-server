package com.diaoji.service;

import com.diaoji.vo.HotSpotVO;
import com.diaoji.vo.NearbySpotVO;
import com.diaoji.vo.SpeciesRankingVO;
import com.diaoji.vo.UserRankingVO;

import java.util.List;
import java.util.Map;

public interface HomeService {

    /** 钓点热度榜 */
    List<HotSpotVO> getSpotHeatRanking(String period);

    /** 用户排行榜 */
    Map<String, Object> getUserRanking(String scope, String regionCode, String dimension, int page, int pageSize);

    /** 鱼种排行 */
    List<SpeciesRankingVO> getSpeciesRanking(int limit);

    /** 附近钓点 */
    Map<String, Object> getNearbySpots(Double lat, Double lng, Double radius, String sortBy, int page, int pageSize);
}
