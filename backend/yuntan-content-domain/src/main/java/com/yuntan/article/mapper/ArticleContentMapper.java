package com.yuntan.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuntan.article.entity.ArticleContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface ArticleContentMapper extends BaseMapper<ArticleContent> {

    /**
     * 根据ID查询文章内容
     */
    @Select("select * from article_content where id = #{articleContentId}")
    Optional<String> getArticleContentById(Long articleContentId);
}
