package com.diaoji.controller;

import com.diaoji.util.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diaoji.entity.FishRecord;
import com.diaoji.service.FishRecordService;
import com.diaoji.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "渔获模块")
@RestController
@RequestMapping("/api/catch")
public class FishRecordController {

    @Autowired
    private FishRecordService fishRecordService;

    @Operation(summary = "记录渔获")
    @PostMapping
    public Result<Long> add(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        Long recordId = fishRecordService.addRecord(userId, params);
        return Result.success(recordId);
    }

    @Operation(summary = "我的渔获列表")
    @GetMapping("/list")
    public Result<Page<FishRecord>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(fishRecordService.getMyRecords(userId, page, size));
    }

    @Operation(summary = "渔获详情")
    @GetMapping("/{id}")
    public Result<FishRecord> detail(@PathVariable Long id) {
        return Result.success(fishRecordService.getDetail(id));
    }

    @Operation(summary = "生成战绩海报")
    @PostMapping("/{id}/poster")
    public Result<String> generatePoster(@PathVariable Long id) {
        String taskId = fishRecordService.generatePoster(id);
        return Result.success(taskId);
    }

    @Operation(summary = "查询海报状态")
    @GetMapping("/poster/{taskId}")
    public Result<String> posterStatus(@PathVariable String taskId) {
        return Result.success(fishRecordService.getPosterStatus(taskId));
    }

    @Operation(summary = "渔获统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = UserContext.getUserId();
        return Result.success(fishRecordService.getStats(userId));
    }
}
