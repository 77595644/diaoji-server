package com.diaoji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diaoji.entity.Comment;
import com.diaoji.entity.Post;
import com.diaoji.entity.User;
import com.diaoji.mapper.PostMapper;
import com.diaoji.mapper.UserMapper;
import com.diaoji.service.CommentService;
import com.diaoji.service.FeedService;
import com.diaoji.util.MinioUploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedServiceImpl extends ServiceImpl<PostMapper, Post> implements FeedService {

    @Autowired
    private MinioUploader minioUploader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Post> getFeed(Integer page, Integer size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> q = new LambdaQueryWrapper<>();
        q.eq(Post::getStatus, 1)
         .orderByDesc(Post::getCreatedAt);
        Page<Post> result = baseMapper.selectPage(p, q);

        // 批量查询用户信息并填充昵称/头像
        Set<Long> userIds = result.getRecords().stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!userIds.isEmpty()) {
            Map<Long, User> userMap = new HashMap<>();
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
            for (Post post : result.getRecords()) {
                User user = userMap.get(post.getUserId());
                if (user != null) {
                    post.setUserNickname(user.getNickname());
                    post.setUserAvatarUrl(user.getAvatarUrl());
                }
            }
        }
        return result;
    }

    @Override
    @Transactional
    public Long publish(Long userId, Map<String, Object> params) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent((String) params.get("content"));
        post.setStatus(1);

        Object photosObj = params.get("photos");
        if (photosObj instanceof List) {
            List<?> photosList = (List<?>) photosObj;
            List<String> uploadedUrls = new ArrayList<>();
            for (Object p : photosList) {
                if (p != null && !p.toString().isEmpty()) {
                    String url = minioUploader.uploadBase64(p.toString(), "feed/");
                    uploadedUrls.add(url);
                }
            }
            if (!uploadedUrls.isEmpty()) {
                try {
                    post.setPhotos(objectMapper.writeValueAsString(uploadedUrls));
                } catch (Exception ignored) {}
            }
        }

        baseMapper.insert(post);
        return post.getId();
    }

    @Override
    public void like(Long userId, Long postId) { /* TODO */ }

    @Override
    public void unlike(Long userId, Long postId) { /* TODO */ }

    @Override
    @Transactional
    public Long comment(Long userId, Long postId, Map<String, Object> params) {
        String content = (String) params.get("content");
        // 兼容 parentId 和 replyToCommentId
        Object parentIdObj = params.get("parentId");
        if (parentIdObj == null) parentIdObj = params.get("replyToCommentId");
        Long parentId = (parentIdObj != null) ? ((Number) parentIdObj).longValue() : 0L;

        // 被回复人的 userId：如果回复的是一级评论，取该评论的 userId
        Long replyToUserId = null;
        if (parentId > 0) {
            Comment parentComment = commentService.getById(parentId);
            if (parentComment != null) {
                replyToUserId = parentComment.getUserId();
            }
        }

        Long commentId = commentService.addComment(userId, postId, parentId, replyToUserId, content);
        baseMapper.incrementCommentCount(postId);
        return commentId;
    }

    @Override
    public List<Map<String, Object>> getComments(Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
        boolean ok = commentService.deleteComment(commentId, userId);
        if (ok) {
            baseMapper.decrementCommentCount(postId);
        }
    }

    @Override
    public void follow(Long followerId, Long followeeId) { /* TODO */ }
}
