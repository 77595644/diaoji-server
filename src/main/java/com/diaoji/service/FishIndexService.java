package com.diaoji.service;

import java.math.BigDecimal;
import java.util.Map;

public interface FishIndexService {

    /** 今日钓鱼指数（简化版）*/
    Map<String, Object> getTodayIndex(BigDecimal lat, BigDecimal lng);

    /** 鱼情指数详情 */
    Map<String, Object> getIndexDetail(BigDecimal lat, BigDecimal lng);
}
