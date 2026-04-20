package com.diaoji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.diaoji.entity.Post;
import java.util.List;
import java.util.Map;

public interface FeedService extends IService<Post> {

    Page<Post> getFeed(Integer page, Integer size);

    Long publish(Long userId, Map<String, Object> params);

    void like(Long userId, Long postId);

    void unlike(Long userId, Long postId);

    Long comment(Long userId, Long postId, Map<String, Object> params);

    List<Map<String, Object>> getComments(Long postId);

    void deleteComment(Long userId, Long postId, Long commentId);

    void follow(Long followerId, Long followeeId);
}
