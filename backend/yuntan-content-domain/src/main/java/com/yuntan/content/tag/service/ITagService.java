package com.yuntan.content.tag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.content.tag.dto.TagDTO;
import com.yuntan.content.tag.dto.TagStatusDTO;
import com.yuntan.content.tag.dto.TagUpdateDTO;
import com.yuntan.content.tag.entity.Tag;
import com.yuntan.content.tag.vo.TagContentVO;
import com.yuntan.content.tag.vo.TagFrontVO;
import com.yuntan.content.tag.vo.TagVO;

import java.util.List;

public interface ITagService extends IService<Tag> {

    /**
     * 获取所有标签名称
     */
    List<TagFrontVO> getAllTagNames();

    /**
     * 获取所有标签
     */
    List<TagVO> getAdminTag();

    /**
     * 添加标签
     */
    void addTag(TagDTO tagDTO);

    /**
     * 修改标签
     */
    void updateTag(TagUpdateDTO tagUpdateDTO);

    /**
     * 根据id获取标签内容
     */
    TagContentVO getTagById(Long id);

    /**
     * 修改标签状态
     */
    void changeTagStatus(TagStatusDTO tagStatusDTO);
}
