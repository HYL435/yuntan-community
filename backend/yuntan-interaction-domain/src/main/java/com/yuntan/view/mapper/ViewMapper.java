package com.yuntan.view.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuntan.view.entity.ArticleView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ViewMapper extends BaseMapper<ArticleView> {

    /**
     * 添加文章浏览
     */
    @Insert("insert into article_view (user_id, article_id, ip, create_time) values (#{userId}, #{articleId},#{ip}, #{createTime})")
    void addView(ArticleView view);
}
