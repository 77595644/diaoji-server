package com.diaoji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diaoji.entity.Post;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("UPDATE t_post SET comment_count = comment_count + 1 WHERE id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId);

    @Update("UPDATE t_post SET comment_count = comment_count - 1 WHERE id = #{postId} AND comment_count > 0")
    int decrementCommentCount(@Param("postId") Long postId);
}
