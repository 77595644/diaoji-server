package com.diaoji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.diaoji.entity.Comment;
import java.util.List;
import java.util.Map;

public interface CommentService extends IService<Comment> {

    Long addComment(Long userId, Long postId, Long parentId, Long replyToUserId, String content);

    List<Map<String, Object>> getCommentsByPostId(Long postId);

    boolean deleteComment(Long commentId, Long userId);

    boolean decrementPostCommentCount(Long postId);
}
