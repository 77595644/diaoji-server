package com.diaoji.mapper;

import com.diaoji.vo.HotSpotVO;
import com.diaoji.vo.NearbySpotVO;
import com.diaoji.vo.SpeciesRankingVO;
import com.diaoji.vo.UserRankingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeMapper {

    // ==================== 钓点热度榜 ====================

    @Select("""
        SELECT
            r.spot_id      AS spotId,
            s.name         AS spotName,
            s.city         AS city,
            COUNT(r.id)    AS heatScore
        FROM t_fish_record r
        JOIN t_fishing_spot s ON r.spot_id = s.id
        WHERE r.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
          AND r.deleted = 0 AND s.deleted = 0
        GROUP BY r.spot_id, s.name, s.city
        ORDER BY heatScore DESC
        LIMIT 10
        """)
    List<HotSpotVO> listSpotHeatMonth30();

    @Select("""
        SELECT
            r.spot_id      AS spotId,
            s.name         AS spotName,
            s.city         AS city,
            COUNT(r.id)    AS heatScore
        FROM t_fish_record r
        JOIN t_fishing_spot s ON r.spot_id = s.id
        WHERE YEAR(r.created_at) = YEAR(CURDATE())
          AND MONTH(r.created_at) = MONTH(CURDATE())
          AND r.deleted = 0 AND s.deleted = 0
        GROUP BY r.spot_id, s.name, s.city
        ORDER BY heatScore DESC
        LIMIT 10
        """)
    List<HotSpotVO> listSpotHeatCurrentMonth();

    // ==================== 用户排行榜 ====================

    @Select("""
        SELECT
            u.id           AS userId,
            u.nickname     AS nickname,
            u.avatar_url   AS avatarUrl,
            SUM(r.weight)  AS value,
            'weight'       AS dimension
        FROM t_fish_record r
        JOIN t_user u ON r.user_id = u.id
        WHERE r.deleted = 0 AND u.deleted = 0
        GROUP BY u.id, u.nickname, u.avatar_url
        ORDER BY value DESC
        LIMIT 100
        """)
    List<UserRankingVO> listUserRankingByWeight();

    @Select("""
        SELECT
            u.id           AS userId,
            u.nickname     AS nickname,
            u.avatar_url   AS avatarUrl,
            SUM(r.quantity) AS value,
            'count'        AS dimension
        FROM t_fish_record r
        JOIN t_user u ON r.user_id = u.id
        WHERE r.deleted = 0 AND u.deleted = 0
        GROUP BY u.id, u.nickname, u.avatar_url
        ORDER BY value DESC
        LIMIT 100
        """)
    List<UserRankingVO> listUserRankingByCount();

    @Select("""
        SELECT
            u.id               AS userId,
            u.nickname         AS nickname,
            u.avatar_url       AS avatarUrl,
            COUNT(DISTINCT DATE(r.created_at)) AS value,
            'trips'            AS dimension
        FROM t_fish_record r
        JOIN t_user u ON r.user_id = u.id
        WHERE r.deleted = 0 AND u.deleted = 0
        GROUP BY u.id, u.nickname, u.avatar_url
        ORDER BY value DESC
        LIMIT 100
        """)
    List<UserRankingVO> listUserRankingByTrips();

    // ==================== 鱼种排行 ====================

    @Select("""
        SELECT
            fish_species   AS speciesName,
            SUM(weight)    AS totalWeight,
            SUM(quantity)  AS totalCount
        FROM t_fish_record
        WHERE deleted = 0 AND fish_species IS NOT NULL AND fish_species != ''
        GROUP BY fish_species
        ORDER BY totalWeight DESC
        LIMIT #{limit}
        """)
    List<SpeciesRankingVO> listSpeciesRanking(@Param("limit") int limit);

    // ==================== 附近钓点（XML 实现） ====================
    // listNearbyByDistance / listNearbyByHeat 已在 HomeMapper.xml 中定义
    List<NearbySpotVO> listNearbyByDistance(
            @Param("lat") Double lat, @Param("lng") Double lng,
            @Param("radiusKm") Double radiusKm,
            @Param("offset") int offset, @Param("limit") int limit);

    List<NearbySpotVO> listNearbyByHeat(
            @Param("lat") Double lat, @Param("lng") Double lng,
            @Param("radiusKm") Double radiusKm,
            @Param("offset") int offset, @Param("limit") int limit);
}
