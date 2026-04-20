package com.diaoji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diaoji.entity.SpotReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpotReviewMapper extends BaseMapper<SpotReview> {

    @Select("SELECT AVG(rating) FROM t_spot_review WHERE spot_id = #{spotId} AND deleted = 0")
    Double selectAvgRating(@Param("spotId") Long spotId);
}
