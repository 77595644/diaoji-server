package com.diaoji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diaoji.entity.Comment;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.nickname, u.avatar_url AS avatarUrl " +
            "FROM t_comment c " +
            "LEFT JOIN t_user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.parent_id = 0 AND c.deleted = 0 " +
            "ORDER BY c.created_at DESC")
    java.util.List<java.util.Map<String, Object>> selectTopLevelComments(@Param("postId") Long postId);

    @Select("SELECT c.*, u.nickname, u.avatar_url AS avatarUrl, " +
            "ru.nickname AS replyToNickname " +
            "FROM t_comment c " +
            "LEFT JOIN t_user u ON c.user_id = u.id " +
            "LEFT JOIN t_user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.parent_id = #{parentId} AND c.deleted = 0 " +
            "ORDER BY c.created_at ASC")
    java.util.List<java.util.Map<String, Object>> selectReplies(@Param("parentId") Long parentId);

    @Update("UPDATE t_post SET comment_count = comment_count - 1 " +
            "WHERE id = #{postId} AND comment_count > 0")
    int decrementPostCommentCount(@Param("postId") Long postId);
}
