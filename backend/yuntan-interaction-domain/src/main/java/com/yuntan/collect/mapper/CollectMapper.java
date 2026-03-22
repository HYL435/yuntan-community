package com.yuntan.collect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuntan.collect.entity.ArticleCollect;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CollectMapper extends BaseMapper<ArticleCollect> {

    /**
     * 判断用户是否收藏
     */
    @Select("select count(*) from article_collect where article_id = #{articleId} and user_id = #{userId}")
    int isCollect(Long articleId, Long userId);

    /**
     * 添加文章收藏
     */
    @Insert("insert into article_collect (user_id, article_id, create_time) values (#{userId}, #{articleId}, #{createTime})")
    void collect(ArticleCollect collect);

    /**
     * 删除文章收藏
     */
    @Delete("delete from article_collect where article_id = #{articleId} and user_id = #{currentId}")
    void deleteCollect(Long articleId, Long currentId);

}
