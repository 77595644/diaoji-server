package com.diaoji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diaoji.entity.FishRecord;
import com.diaoji.mapper.FishRecordMapper;
import com.diaoji.service.FishRecordService;
import com.diaoji.util.MinioUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;

@Service
public class FishRecordServiceImpl extends ServiceImpl<FishRecordMapper, FishRecord>
        implements FishRecordService {

    @Autowired
    private MinioUploader minioUploader;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long addRecord(Long userId, Map<String, Object> params) {
        FishRecord record = new FishRecord();
        record.setUserId(userId);

        // 鱼种
        record.setFishSpecies((String) params.get("fishSpecies"));

        // 重量
        Object weightObj = params.get("weight");
        if (weightObj != null) {
            if (weightObj instanceof Number) {
                record.setWeight(new BigDecimal(weightObj.toString()));
            } else {
                try {
                    record.setWeight(new BigDecimal(weightObj.toString()));
                } catch (Exception ignored) {}
            }
        }

        // 长度
        Object lengthObj = params.get("length");
        if (lengthObj != null) {
            try {
                record.setLength(new BigDecimal(lengthObj.toString()));
            } catch (Exception ignored) {}
        }

        // 数量
        Object qtyObj = params.get("quantity");
        if (qtyObj != null) {
            record.setQuantity(((Number) qtyObj).intValue());
        } else {
            record.setQuantity(1);
        }

        // 钓点
        Object spotIdObj = params.get("spotId");
        if (spotIdObj != null) {
            record.setSpotId(((Number) spotIdObj).longValue());
        }
        record.setSpotName((String) params.get("spotName"));

        // GPS 坐标
        Object gpsLatObj = params.get("gpsLatitude");
        Object gpsLngObj = params.get("gpsLongitude");
        if (gpsLatObj != null && gpsLngObj != null) {
            try {
                record.setGpsLatitude(new BigDecimal(gpsLatObj.toString()));
                record.setGpsLongitude(new BigDecimal(gpsLngObj.toString()));
            } catch (Exception ignored) {}
        }

        // 天气
        Object weatherObj = params.get("weather");
        if (weatherObj != null) {
            record.setWeatherJson(weatherObj.toString());
        }

        // 感受
        record.setFishFeeling((String) params.get("fishFeeling"));

        // 备注
        record.setNote((String) params.get("note"));

        // 图片上传
        Object photoUrlObj = params.get("photoUrl");
        if (photoUrlObj != null && !photoUrlObj.toString().isEmpty()) {
            String base64 = photoUrlObj.toString();
            String url = minioUploader.uploadBase64(base64, "catch/");
            record.setPhotoUrl(url);
        }

        // 多图（暂存 JSON）
        Object photosObj = params.get("photos");
        if (photosObj instanceof List) {
            List<?> photosList = (List<?>) photosObj;
            List<String> uploadedUrls = new ArrayList<>();
            for (Object p : photosList) {
                if (p != null && !p.toString().isEmpty()) {
                    String url = minioUploader.uploadBase64(p.toString(), "catch/");
                    uploadedUrls.add(url);
                }
            }
            if (!uploadedUrls.isEmpty()) {
                try { record.setPhotos(objectMapper.writeValueAsString(uploadedUrls)); } catch (Exception e) {}
            }
        }

        baseMapper.insert(record);
        return record.getId();
    }

    @Override
    public Page<FishRecord> getMyRecords(Long userId, Integer page, Integer size) {
        Page<FishRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<FishRecord> q = new LambdaQueryWrapper<>();
        q.eq(FishRecord::getUserId, userId)
         .orderByDesc(FishRecord::getCreatedAt);
        return baseMapper.selectPage(p, q);
    }

    @Override
    public FishRecord getDetail(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public String generatePoster(Long recordId) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public String getPosterStatus(String taskId) {
        return "processing";
    }

    @Override
    public Map<String, Object> getStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 总出钓次数
        LambdaQueryWrapper<FishRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishRecord::getUserId, userId);
        Long totalTrips = baseMapper.selectCount(wrapper);

        // 总渔获数量
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishRecord::getUserId, userId)
               .select(FishRecord::getQuantity);
        List<FishRecord> records = baseMapper.selectList(wrapper);
        int totalFishCount = records.stream()
            .filter(r -> r.getQuantity() != null)
            .mapToInt(FishRecord::getQuantity)
            .sum();

        // 总重量
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishRecord::getUserId, userId)
               .select(FishRecord::getWeight);
        records = baseMapper.selectList(wrapper);
        BigDecimal totalWeight = records.stream()
            .filter(r -> r.getWeight() != null)
            .map(FishRecord::getWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月出钓
        java.time.LocalDateTime monthStart = java.time.LocalDateTime.now()
            .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FishRecord::getUserId, userId)
               .ge(FishRecord::getCreatedAt, monthStart);
        Long thisMonthTrips = baseMapper.selectCount(wrapper);

        stats.put("totalTrips", totalTrips);
        stats.put("totalFishCount", totalFishCount);
        stats.put("totalWeight", totalWeight);
        stats.put("thisMonthTrips", thisMonthTrips);
        return stats;
    }
}
