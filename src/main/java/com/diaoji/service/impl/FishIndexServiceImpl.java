package com.diaoji.service.impl;

import com.diaoji.service.FishIndexService;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class FishIndexServiceImpl implements FishIndexService {

    @Value("${qweather.api-key:TEST_KEY}")
    private String weatherKey;

    @Value("${qqmap.api-key:jCc5uAh6PE1KtkqpPNptBjEXG5GTIg2N}")
    private String qqMapKey;

    @Override
    public Map<String, Object> getTodayIndex(BigDecimal lat, BigDecimal lng) {
        String cacheKey = "fish-index:" + lat.setScale(2, RoundingMode.HALF_UP)
                + ":" + lng.setScale(2, RoundingMode.HALF_UP) + ":" + LocalDate.now();

        Map<String, Object> result = calculateIndex(lat, lng);
        return result;
    }

    @Override
    public Map<String, Object> getIndexDetail(BigDecimal lat, BigDecimal lng) {
        Map<String, Object> detail = new LinkedHashMap<>(getTodayIndex(lat, lng));
        detail.put("pressureDesc", "气压稳定，适合出钓");
        detail.put("waterTempDesc", "水温适宜，鱼类活跃");
        detail.put("moonDesc", "上弦月，鱼情较好");
        detail.put("weatherDesc", "晴朗天气，钓鱼指数较高");
        detail.put("windDesc", "微风2-3级，适宜出钓");
        detail.put("location", getLocationName(lat, lng));
        return detail;
    }

    /** 逆地理编码：经纬度 → 地址名称 */
    private String getLocationName(BigDecimal lat, BigDecimal lng) {
        try {
            String url = "https://apis.map.qq.com/ws/geocoder/v1/"
                    + "?location=" + lat + "," + lng
                    + "&key=" + qqMapKey
                    + "&get_poi=0";
            String resp = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(resp);
            if ("0".equals(String.valueOf(json.get("status")))) {
                JSONObject result = json.getJSONObject("result");
                if (result != null) {
                    JSONObject adInfo = result.getJSONObject("ad_info");
                    if (adInfo != null) {
                        String city = adInfo.getStr("city", "");
                        String district = adInfo.getStr("district", "");
                        return city + (district.isEmpty() ? "" : district);
                    }
                }
            }
        } catch (Exception e) {
            // 逆地理编码失败不影响主流程
        }
        return lat.setScale(2, RoundingMode.HALF_UP) + "," + lng.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> calculateIndex(BigDecimal lat, BigDecimal lng) {
        // TODO: 接入和风天气API计算真实指数
        Random random = new Random();
        double totalScore = 6.0 + random.nextDouble() * 4;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalScore", Math.round(totalScore * 10) / 10.0);
        result.put("level", getLevel(totalScore));
        result.put("pressureScore", 7.5);
        result.put("waterTempScore", 8.0);
        result.put("moonScore", 7.0);
        result.put("weatherScore", 8.5);
        result.put("windScore", 7.0);
        result.put("location", getLocationName(lat, lng));
        result.put("date", LocalDate.now().toString());
        return result;
    }

    private String getLevel(double score) {
        if (score >= 8) return "极佳";
        if (score >= 6) return "较好";
        if (score >= 4) return "一般";
        if (score >= 2) return "较差";
        return "极差";
    }
}
