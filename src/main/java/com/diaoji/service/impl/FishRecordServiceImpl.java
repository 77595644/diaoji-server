package com.diaoji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diaoji.entity.FishRecord;
import com.diaoji.entity.FishingSpot;
import com.diaoji.mapper.FishRecordMapper;
import com.diaoji.mapper.FishingSpotMapper;
import com.diaoji.service.FishRecordService;
import com.diaoji.util.MinioUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Autowired
    private FishingSpotMapper spotMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Long addRecord(Long userId, Map<String, Object> params) {
        // —— 参数校验 ——
        String fishSpecies = (String) params.get("fishSpecies");
        if (fishSpecies == null || fishSpecies.trim().isEmpty()) {
            throw new IllegalArgumentException("鱼种不能为空");
        }

        FishRecord record = new FishRecord();
        record.setUserId(userId);
        record.setFishSpecies(fishSpecies.trim());

        // 重量（可空，非法值降为 null）
        Object weightObj = params.get("weight");
        if (weightObj != null) {
            try {
                String wStr = weightObj.toString().trim();
                if (!wStr.isEmpty()) {
                    BigDecimal w = new BigDecimal(wStr);
                    if (w.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("重量必须大于0");
                    record.setWeight(w);
                }
            } catch (IllegalArgumentException e) { throw e; }
            catch (Exception ignored) { /* 非法格式降为 null */ }
        }

        // 长度（可空，非法值降为 null）
        Object lengthObj = params.get("length");
        if (lengthObj != null) {
            try {
                String lStr = lengthObj.toString().trim();
                if (!lStr.isEmpty()) {
                    BigDecimal l = new BigDecimal(lStr);
                    if (l.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("长度必须大于0");
                    record.setLength(l);
                }
            } catch (IllegalArgumentException e) { throw e; }
            catch (Exception ignored) { /* 非法格式降为 null */ }
        }

        // 数量（默认 1）
        Object qtyObj = params.get("quantity");
        if (qtyObj != null) {
            try {
                int qty = ((Number) qtyObj).intValue();
                if (qty < 1) qty = 1;
                record.setQuantity(qty);
            } catch (Exception e) {
                record.setQuantity(1);
            }
        } else {
            record.setQuantity(1);
        }

        // 钓点（可空，ID 存在性校验）
        Object spotIdObj = params.get("spotId");
        if (spotIdObj != null) {
            try {
                Long spotId = ((Number) spotIdObj).longValue();
                FishingSpot spot = spotMapper.selectById(spotId);
                // selectById 已由 @TableLogic 自动过滤 deleted=1 记录
                if (spot != null) {
                    record.setSpotId(spotId);
                }
                // spotId 无效 → 静默忽略，不阻断提交
            } catch (Exception ignored) {}
        }
        // spotName 若传了则同步存储（可空）
        String spotName = (String) params.get("spotName");
        if (spotName != null && !spotName.trim().isEmpty()) {
            record.setSpotName(spotName.trim());
        }

        // GPS 坐标（可空，非法值降为 null）
        Object gpsLatObj = params.get("gpsLatitude");
        Object gpsLngObj = params.get("gpsLongitude");
        if (gpsLatObj != null && gpsLngObj != null) {
            try {
                BigDecimal lat = new BigDecimal(gpsLatObj.toString());
                BigDecimal lng = new BigDecimal(gpsLngObj.toString());
                if (lat.abs().compareTo(new BigDecimal("90")) <= 0
                    && lng.abs().compareTo(new BigDecimal("180")) <= 0) {
                    record.setGpsLatitude(lat);
                    record.setGpsLongitude(lng);
                }
            } catch (Exception ignored) {}
        }

        // 天气（可空）
        Object weatherObj = params.get("weather");
        if (weatherObj != null && !weatherObj.toString().trim().isEmpty()) {
            record.setWeatherJson(weatherObj.toString().trim());
        }

        // 感受（可空）
        String fishFeeling = (String) params.get("fishFeeling");
        if (fishFeeling != null && !fishFeeling.trim().isEmpty()) {
            record.setFishFeeling(fishFeeling.trim());
        }

        // 备注（可空，最大 100 字符）
        String note = (String) params.get("note");
        if (note != null && !note.trim().isEmpty()) {
            if (note.length() > 100) note = note.substring(0, 100);
            record.setNote(note.trim());
        }

        // —— 图片上传（事务保护：同一事务内，失败则整体回滚）——
        // 优先取 photos 数组（多图），fallback 到 photoUrl（单图）
        Object photoUrlObj = params.get("photoUrl");
        Object photosObj = params.get("photos");
        List<String> uploadedUrls = new ArrayList<>();

        if (photosObj instanceof List && !((List<?>) photosObj).isEmpty()) {
            // 多图模式：逐一上传，全部成功才算成功
            List<?> photosList = (List<?>) photosObj;
            for (Object p : photosList) {
                if (p != null && !p.toString().trim().isEmpty()) {
                    String url = minioUploader.uploadBase64(p.toString().trim(), "catch/");
                    uploadedUrls.add(url);
                }
            }
        } else if (photoUrlObj != null && !photoUrlObj.toString().trim().isEmpty()) {
            // 单图模式
            String url = minioUploader.uploadBase64(photoUrlObj.toString().trim(), "catch/");
            uploadedUrls.add(url);
        }

        // 写入 DB
        if (!uploadedUrls.isEmpty()) {
            record.setPhotoUrl(uploadedUrls.get(0));           // 主图
            try {
                record.setPhotos(objectMapper.writeValueAsString(uploadedUrls)); // 完整列表
            } catch (Exception e) {
                // JSON 序列化失败不影响主图
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

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateRecord(Long userId, Long recordId, Map<String, Object> params) {
        FishRecord record = baseMapper.selectById(recordId);
        if (record == null) throw new IllegalArgumentException("渔获记录不存在");
        if (!userId.equals(record.getUserId())) throw new SecurityException("无权修改此记录");

        // 鱼种（必填）
        if (params.containsKey("fishSpecies")) {
            String fs = (String) params.get("fishSpecies");
            if (fs == null || fs.trim().isEmpty()) throw new IllegalArgumentException("鱼种不能为空");
            record.setFishSpecies(fs.trim());
        }

        // 重量（可空，非法值降为 null）
        if (params.containsKey("weight")) {
            Object wObj = params.get("weight");
            if (wObj == null || wObj.toString().trim().isEmpty()) {
                record.setWeight(null);
            } else {
                try {
                    BigDecimal w = new BigDecimal(wObj.toString().trim());
                    if (w.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("重量必须大于0");
                    record.setWeight(w);
                } catch (IllegalArgumentException e) { throw e; }
                catch (Exception e) { record.setWeight(null); }
            }
        }

        // 长度（可空）
        if (params.containsKey("length")) {
            Object lObj = params.get("length");
            if (lObj == null || lObj.toString().trim().isEmpty()) {
                record.setLength(null);
            } else {
                try {
                    BigDecimal l = new BigDecimal(lObj.toString().trim());
                    if (l.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("长度必须大于0");
                    record.setLength(l);
                } catch (IllegalArgumentException e) { throw e; }
                catch (Exception e) { record.setLength(null); }
            }
        }

        // 数量
        if (params.containsKey("quantity")) {
            try {
                int qty = ((Number) params.get("quantity")).intValue();
                record.setQuantity(qty < 1 ? 1 : qty);
            } catch (Exception e) { record.setQuantity(1); }
        }

        // 钓点（可空）
        if (params.containsKey("spotId")) {
            Object sidObj = params.get("spotId");
            if (sidObj != null) {
                try {
                    Long sid = ((Number) sidObj).longValue();
                    FishingSpot spot = spotMapper.selectById(sid);
                    // selectById 已由 @TableLogic 自动过滤 deleted=1 记录
                    if (spot != null) record.setSpotId(sid);
                } catch (Exception ignored) {}
            } else {
                record.setSpotId(null);
            }
        }

        if (params.containsKey("spotName")) {
            String sn = (String) params.get("spotName");
            record.setSpotName(sn != null && !sn.trim().isEmpty() ? sn.trim() : null);
        }

        // GPS
        if (params.containsKey("gpsLatitude")) {
            try {
                Object latObj = params.get("gpsLatitude");
                Object lngObj = params.get("gpsLongitude");
                if (latObj != null && lngObj != null) {
                    BigDecimal lat = new BigDecimal(latObj.toString());
                    BigDecimal lng = new BigDecimal(lngObj.toString());
                    if (lat.abs().compareTo(new BigDecimal("90")) <= 0
                        && lng.abs().compareTo(new BigDecimal("180")) <= 0) {
                        record.setGpsLatitude(lat);
                        record.setGpsLongitude(lng);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (params.containsKey("weather")) {
            String w = (String) params.get("weather");
            record.setWeatherJson(w != null && !w.trim().isEmpty() ? w.trim() : null);
        }
        if (params.containsKey("fishFeeling")) {
            String ff = (String) params.get("fishFeeling");
            record.setFishFeeling(ff != null && !ff.trim().isEmpty() ? ff.trim() : null);
        }
        if (params.containsKey("note")) {
            String note = (String) params.get("note");
            record.setNote(note != null && !note.trim().isEmpty()
                ? (note.length() > 100 ? note.substring(0, 100) : note.trim()) : null);
        }

        // 图片（事务内：DB update 失败则图片不保存）
        if (params.containsKey("photoUrl")) {
            String url = params.get("photoUrl").toString();
            if (!url.isEmpty() && !url.startsWith("http")) {
                // base64 → MinIO → URL
                url = minioUploader.uploadBase64(url.trim(), "catch/");
            }
            record.setPhotoUrl(url.isEmpty() ? null : url);
        }

        baseMapper.updateById(record);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteRecord(Long userId, Long recordId) {
        FishRecord record = baseMapper.selectById(recordId);
        if (record == null) throw new IllegalArgumentException("渔获记录不存在");
        if (!userId.equals(record.getUserId())) throw new SecurityException("无权删除此记录");
        baseMapper.deleteById(recordId);
    }
}
