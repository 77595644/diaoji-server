package com.diaoji.controller;

import com.diaoji.service.FishIndexService;
import com.diaoji.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "鱼情模块")
@RestController
@RequestMapping("/api/fish-index")
public class FishIndexController {

    @Autowired
    private FishIndexService fishIndexService;

    @Operation(summary = "今日钓鱼指数")
    @GetMapping("/today")
    public Result<Map<String, Object>> today(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng) {
        return Result.success(fishIndexService.getTodayIndex(lat, lng));
    }

    @Operation(summary = "鱼情指数详情")
    @GetMapping("/detail")
    public Result<Map<String, Object>> detail(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng) {
        return Result.success(fishIndexService.getIndexDetail(lat, lng));
    }
}
