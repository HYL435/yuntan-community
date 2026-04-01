package com.yuntan.content.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuntan.content.tag.vo.TagFrontVO;
import com.yuntan.content.tag.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据文章ID获取文章标签
     */
    @Select("select t.tag_name from tag t join article_tag at on t.id = at.tag_id where at.article_id = #{articleId}")
    List<String> getTagsByArticleId(Long articleId);

    /**
     * 获取所有标签名称
     */
    @Select("select id,tag_name from tag where status = 1 and deleted = 0")
    List<TagFrontVO> getTags();
}
