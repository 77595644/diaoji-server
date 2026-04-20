package com.diaoji.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.diaoji.entity.FishRecord;

import java.util.Map;

public interface FishRecordService extends IService<FishRecord> {

    /** 记录渔获 */
    Long addRecord(Long userId, Map<String, Object> params);

    /** 我的渔获列表 */
    Page<FishRecord> getMyRecords(Long userId, Integer page, Integer size);

    /** 渔获详情 */
    FishRecord getDetail(Long id);

    /** 生成战绩海报（异步）*/
    String generatePoster(Long recordId);

    /** 查询海报生成状态 */
    String getPosterStatus(String taskId);

    /** 渔获统计 */
    Map<String, Object> getStats(Long userId);
}
