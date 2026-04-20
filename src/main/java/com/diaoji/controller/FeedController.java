package com.diaoji.controller;

import com.diaoji.util.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diaoji.entity.Post;
import com.diaoji.service.FeedService;
import com.diaoji.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "社交模块")
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @Operation(summary = "Feed流（纯时间线）")
    @GetMapping
    public Result<Page<Post>> feed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(feedService.getFeed(page, size));
    }

    @Operation(summary = "发布动态")
    @PostMapping("/post")
    public Result<Long> publish(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        Long postId = feedService.publish(userId, params);
        return Result.success(postId);
    }

    @Operation(summary = "点赞")
    @PostMapping("/post/{postId}/like")
    public Result<Void> like(@PathVariable Long postId) {
        Long userId = UserContext.getUserId();
        feedService.like(userId, postId);
        return Result.success();
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/post/{postId}/like")
    public Result<Void> unlike(@PathVariable Long postId) {
        Long userId = UserContext.getUserId();
        feedService.unlike(userId, postId);
        return Result.success();
    }

    @Operation(summary = "评论")
    @PostMapping("/post/{postId}/comment")
    public Result<Long> comment(@PathVariable Long postId,
                                @RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        Long commentId = feedService.comment(userId, postId, params);
        return Result.success(commentId);
    }

    @Operation(summary = "评论列表")
    @GetMapping("/post/{postId}/comments")
    public Result<List<Map<String, Object>>> getComments(@PathVariable Long postId) {
        return Result.success(feedService.getComments(postId));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/post/{postId}/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long postId,
                                      @PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        feedService.deleteComment(userId, postId, commentId);
        return Result.success();
    }

    @Operation(summary = "关注用户")
    @PostMapping("/user/{userId}/follow")
    public Result<Void> follow(@PathVariable Long userId) {
        Long myId = UserContext.getUserId();
        feedService.follow(myId, userId);
        return Result.success();
    }
}
