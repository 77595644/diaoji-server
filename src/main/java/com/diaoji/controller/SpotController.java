package com.diaoji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diaoji.entity.FishingSpot;
import com.diaoji.service.FishingSpotService;
import com.diaoji.util.UserContext;
import com.diaoji.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "钓点模块")
@RestController
@RequestMapping("/api/spot")
public class SpotController {

    @Autowired
    private FishingSpotService spotService;

    @Operation(summary = "附近钓点（支持zoom级别去重）")
    @GetMapping("/nearby")
    public Result<Page<FishingSpot>> nearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "5000") Integer radius,
            @RequestParam(defaultValue = "12") Integer zoom,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // zoom 限制最小6，默认12
        if (zoom < 6) zoom = 6;
        return Result.success(spotService.queryNearby(lat, lng, radius, zoom, page, size));
    }

    @Operation(summary = "搜索钓点")
    @GetMapping("/search")
    public Result<List<FishingSpot>> search(@RequestParam String keyword) {
        return Result.success(spotService.searchByKeyword(keyword));
    }

    @Operation(summary = "钓点详情")
    @GetMapping("/{id}")
    public Result<FishingSpot> detail(@PathVariable Long id) {
        return Result.success(spotService.getSpotDetail(id));
    }

    @Operation(summary = "我的钓点列表")
    @GetMapping("/my")
    public Result<Page<FishingSpot>> myList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(spotService.getMySpots(userId, page, size));
    }

    @Operation(summary = "上报新钓点")
    @PostMapping
    public Result<Void> add(@RequestBody FishingSpot spot) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        spot.setCreatedBy(userId);
        spot.setStatus(1); // 0=待审核 1=已上线
        spotService.addSpot(spot);
        return Result.success();
    }

    @Operation(summary = "评价钓点")
    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String content) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        spotService.addReview(id, userId, rating, content);
        return Result.success();
    }
}
