package com.diaoji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diaoji.entity.FishingSpot;
import com.diaoji.entity.SpotReview;
import com.diaoji.mapper.FishingSpotMapper;
import com.diaoji.mapper.SpotReviewMapper;
import com.diaoji.service.FishingSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FishingSpotServiceImpl extends ServiceImpl<FishingSpotMapper, FishingSpot>
        implements FishingSpotService {

    @Autowired
    private SpotReviewMapper reviewMapper;

    /**
     * zoom → geohash 前缀位数映射
     * zoom < 6 → 2位（国家级，~1100km格子）
     * zoom 6-8  → 3位（省级，~120km格子）
     * zoom 9-11 → 4位（市级，~20km格子）
     * zoom 12-14→ 5位（区级，~2.4km格子）
     * zoom >= 15 → 不分组（精确点）
     */
    private int zoomToGeohashPrefix(Integer zoom) {
        if (zoom == null || zoom < 6) return 2;
        if (zoom <= 8) return 3;
        if (zoom <= 11) return 4;
        if (zoom <= 14) return 5;
        return 0; // 0 = 不分组，精确返回
    }

    @Override
    public Page<FishingSpot> queryNearby(BigDecimal lat, BigDecimal lng,
                                         Integer radius, Integer zoom,
                                         Integer page, Integer size) {
        int prefix = zoomToGeohashPrefix(zoom);

        // zoom >= 15：精确查询，不走 geohash 分组
        if (prefix == 0) {
            return queryNearbyExact(lat, lng, radius, page, size);
        }

        // zoom < 15：按 geohash 前缀分组，每组取 total_records 最大的一条
        return queryNearbyGrouped(lat, lng, radius, prefix, page, size);
    }

    /**
     * 精确查询：直接按矩形范围 + 距离排序
     */
    private Page<FishingSpot> queryNearbyExact(BigDecimal lat, BigDecimal lng,
                                                Integer radius, Integer page, Integer size) {
        double latDelta = radius / 111000.0;
        double lngDelta = latDelta / Math.cos(lat.doubleValue());

        double lngMin = lng.subtract(BigDecimal.valueOf(lngDelta)).doubleValue();
        double lngMax = lng.add(BigDecimal.valueOf(lngDelta)).doubleValue();

        LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishingSpot::getStatus, 1)
               .between(FishingSpot::getLatitude,
                   lat.subtract(BigDecimal.valueOf(latDelta)),
                   lat.add(BigDecimal.valueOf(latDelta)))
               .between(FishingSpot::getLongitude,
                   Math.min(lngMin, lngMax), Math.max(lngMin, lngMax))
               .orderByDesc(FishingSpot::getTotalRecords);

        Page<FishingSpot> result = page(new Page<>(page, size), wrapper);
        fillDistance(result.getRecords(), lat.doubleValue(), lng.doubleValue());
        return result;
    }

    /**
     * 分组查询：按 geohash 前缀分组去重，每组取热度最高的一条
     * 使用子查询方式：先查分组内最大 id，再按 id 批量查详情
     */
    private Page<FishingSpot> queryNearbyGrouped(BigDecimal lat, BigDecimal lng,
                                                  Integer radius, int prefix,
                                                  Integer page, Integer size) {
        double latDelta = radius / 111000.0;
        double lngDelta = latDelta / Math.cos(lat.doubleValue());

        double latMin = lat.subtract(BigDecimal.valueOf(latDelta)).doubleValue();
        double latMax = lat.add(BigDecimal.valueOf(latDelta)).doubleValue();
        double lngMin = lng.subtract(BigDecimal.valueOf(lngDelta)).doubleValue();
        double lngMax = lng.add(BigDecimal.valueOf(lngDelta)).doubleValue();

        // 子查询：每个 geohash 前缀分组中取 total_records 最大的记录 id
        String subSql = String.format(
            "SELECT MAX(id) FROM t_fishing_spot " +
            "WHERE status = 1 AND deleted = 0 " +
            "AND latitude BETWEEN %s AND %s " +
            "AND longitude BETWEEN %s AND %s " +
            "AND geohash IS NOT NULL " +
            "GROUP BY LEFT(geohash, %d)",
            latMin, latMax,
            Math.min(lngMin, lngMax), Math.max(lngMin, lngMax),
            prefix
        );

        // 主查询：按子查询的 id 列表查详情
        LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishingSpot::getStatus, 1)
               .inSql(FishingSpot::getId, subSql)
               .orderByDesc(FishingSpot::getTotalRecords);

        Page<FishingSpot> result = page(new Page<>(page, size), wrapper);
        fillDistance(result.getRecords(), lat.doubleValue(), lng.doubleValue());
        return result;
    }

    /**
     * 填充距离描述（替换 description 字段为 "Xm" 格式）
     */
    private void fillDistance(List<FishingSpot> spots, double lat, double lng) {
        for (FishingSpot spot : spots) {
            if (spot.getLatitude() != null && spot.getLongitude() != null) {
                double d = calcDistance(lat, lng,
                    spot.getLatitude().doubleValue(), spot.getLongitude().doubleValue());
                spot.setDescription(String.valueOf(Math.round(d)) + "m");
            }
        }
    }

    @Override
    public List<FishingSpot> searchByKeyword(String keyword) {
        LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishingSpot::getStatus, 1)
               .and(w -> w.like(FishingSpot::getName, keyword)
                       .or().like(FishingSpot::getDescription, keyword)
                       .or().like(FishingSpot::getCity, keyword))
               .orderByDesc(FishingSpot::getTotalRecords)
               .last("LIMIT 20");
        return list(wrapper);
    }

    @Override
    public FishingSpot getSpotDetail(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public void addSpot(FishingSpot spot) {
        // 将逗号分隔的字符串转为 JSON 数组（如 "路亚,台钓" → ["路亚","台钓"]）
        String styles = spot.getFishingStyles();
        if (styles != null && !styles.isEmpty()) {
            String[] arr = styles.split(",");
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                json.append("\"").append(arr[i].trim()).append("\"");
                if (i < arr.length - 1) json.append(",");
            }
            json.append("]");
            spot.setFishingStyles(json.toString());
        }

        String geohash = encodeGeohash(
            spot.getLatitude().doubleValue(),
            spot.getLongitude().doubleValue(), 6);
        spot.setGeohash(geohash);
        save(spot);
    }

    @Override
    @Transactional
    public void addReview(Long spotId, Long userId, Integer rating, String content) {
        reviewMapper.delete(new LambdaQueryWrapper<SpotReview>()
                .eq(SpotReview::getSpotId, spotId)
                .eq(SpotReview::getUserId, userId));
        SpotReview review = new SpotReview();
        review.setSpotId(spotId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        reviewMapper.insert(review);
        recalcSpotRating(spotId);
    }

    @Override
    public Page<FishingSpot> getMySpots(Long userId, Integer page, Integer size) {
        Page<FishingSpot> p = new Page<>(page, size);
        LambdaQueryWrapper<FishingSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishingSpot::getCreatedBy, userId)
               .eq(FishingSpot::getStatus, 1)
               .orderByDesc(FishingSpot::getCreatedAt);
        return baseMapper.selectPage(p, wrapper);
    }

    private void recalcSpotRating(Long spotId) {
        Double avg = reviewMapper.selectAvgRating(spotId);
        if (avg != null) {
            FishingSpot spot = new FishingSpot();
            spot.setId(spotId);
            spot.setAvgRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            baseMapper.updateById(spot);
        }
    }

    private double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String encodeGeohash(double lat, double lng, int precision) {
        String base32 = "0123456789bcdefghjkmnpqrstuvwxyz";
        double minLat = -90, maxLat = 90;
        double minLng = -180, maxLng = 180;
        StringBuilder hash = new StringBuilder();
        boolean isEven = true;
        int bit = 0, ch = 0;
        while (hash.length() < precision) {
            if (isEven) {
                double mid = (minLng + maxLng) / 2;
                if (lng >= mid) { ch |= (1 << (4 - bit)); minLng = mid; }
                else maxLng = mid;
            } else {
                double mid = (minLat + maxLat) / 2;
                if (lat >= mid) { ch |= (1 << (4 - bit)); minLat = mid; }
                else maxLat = mid;
            }
            isEven = !isEven;
            if (bit < 4) bit++;
            else { hash.append(base32.charAt(ch)); bit = 0; ch = 0; }
        }
        return hash.toString();
    }
}
