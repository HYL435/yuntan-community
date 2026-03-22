package com.yuntan.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuntan.like.entity.ArticleLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LikeMapper extends BaseMapper<ArticleLike> {


    /**
     * 判断用户是否点赞
     */
    @Select("select count(*) from article_like where article_id = #{articleId} and user_id = #{userId}")
    int isLike(Long articleId, Long userId);

    /**
     * 文章点赞
     */
    @Insert("insert into article_like (user_id, article_id, create_time) values (#{userId}, #{articleId}, #{createTime})")
    void like(ArticleLike like);

    /**
     * 删除文章点赞
     */
    @Delete("delete from article_like where article_id = #{articleId} and user_id = #{currentId}")
    void deleteLike(Long articleId, Long currentId);

}
