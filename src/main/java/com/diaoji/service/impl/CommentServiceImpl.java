package com.diaoji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diaoji.entity.Comment;
import com.diaoji.mapper.CommentMapper;
import com.diaoji.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    @Transactional
    public Long addComment(Long userId, Long postId, Long parentId, Long replyToUserId, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId != null ? parentId : 0L);
        comment.setReplyToUserId(replyToUserId);
        comment.setContent(content);
        baseMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public List<Map<String, Object>> getCommentsByPostId(Long postId) {
        List<Map<String, Object>> topLevel = baseMapper.selectTopLevelComments(postId);
        for (Map<String, Object> c : topLevel) {
            Long commentId = ((Number) c.get("id")).longValue();
            List<Map<String, Object>> replies = baseMapper.selectReplies(commentId);
            c.put("replies", replies);
        }
        return topLevel;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = baseMapper.selectById(commentId);
        if (comment == null || !comment.getUserId().equals(userId) || comment.getDeleted() == 1) {
            return false;
        }
        comment.setDeleted(1);
        baseMapper.updateById(comment);
        return true;
    }

    @Override
    @Transactional
    public boolean decrementPostCommentCount(Long postId) {
        return baseMapper.decrementPostCommentCount(postId) > 0;
    }
}
