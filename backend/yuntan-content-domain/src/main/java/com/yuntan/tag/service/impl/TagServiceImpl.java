package com.yuntan.tag.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.tag.dto.TagDTO;
import com.yuntan.tag.dto.TagStatusDTO;
import com.yuntan.tag.dto.TagUpdateDTO;
import com.yuntan.tag.entity.Tag;
import com.yuntan.tag.mapper.TagMapper;
import com.yuntan.tag.service.ITagService;
import com.yuntan.tag.vo.TagContentVO;
import com.yuntan.tag.vo.TagFrontVO;
import com.yuntan.tag.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.yuntan.common.utils.BeanUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    private final TagMapper tagMapper;

    /**
     * 获取所有标签名称
     */
    @Override
    public List<TagFrontVO> getAllTagNames() {

        return tagMapper.getTags();
    }

    /**
     * 获取所有标签名称管理端
     */
    @Override
    public List<TagVO> getAdminTag() {

        List<Tag> tags = this.list();

        return BeanUtils.copyList(tags, TagVO.class);

    }

    /**
     * 添加标签
     */
    @Override
    public void addTag(TagDTO tagDTO) {

        Tag tag = BeanUtils.copyBean(tagDTO, Tag.class);

        this.save(tag);

    }

    /**
     * 修改标签
     */
    @Override
    public void updateTag(TagUpdateDTO tagUpdateDTO) {

        Tag tag = BeanUtils.copyBean(tagUpdateDTO, Tag.class);

        this.updateById(tag);

    }

    /**
     * 根据id获取标签内容
     */
    @Override
    public TagContentVO getTagById(Long id) {

        Tag tag = this.getById(id);

        return BeanUtils.copyBean(tag, TagContentVO.class);
    }

    /**
     * 修改标签状态
     */
    @Override
    public void changeTagStatus(TagStatusDTO tagStatusDTO) {

//        status = Objects.equals(status, StatusConstant.ENABLE) ? StatusConstant.DISABLE : StatusConstant.ENABLE;

        this.lambdaUpdate()
                .eq(Tag::getId, tagStatusDTO.getId())
                .set(Tag::getStatus, tagStatusDTO.getStatus())
                .update();
    }
}
