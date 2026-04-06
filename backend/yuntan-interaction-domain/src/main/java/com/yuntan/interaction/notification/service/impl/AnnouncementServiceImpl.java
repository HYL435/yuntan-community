package com.yuntan.interaction.notification.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.common.utils.BeanUtils;
import com.yuntan.interaction.notification.constant.AnnouncementStatusConstant;
import com.yuntan.interaction.notification.dto.AnnouncementDTO;
import com.yuntan.interaction.notification.dto.AnnouncementStatusDTO;
import com.yuntan.interaction.notification.dto.UpdateAnnouncementDTO;
import com.yuntan.interaction.notification.entity.Announcement;
import com.yuntan.interaction.notification.mapper.AnnouncementMapper;
import com.yuntan.interaction.notification.service.IAnnouncementService;
import com.yuntan.interaction.notification.vo.AdminAnnouncementVO;
import com.yuntan.interaction.notification.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl
        extends ServiceImpl<AnnouncementMapper, Announcement>
        implements IAnnouncementService {

    private final AnnouncementMapper announcementMapper;


    /**
     * 获取最新公告
     */
    @Override
    public AnnouncementVO getLatestAnnouncement() {

        // 查询最新公告：状态为1（已发布），按照发布时间降序，分页查询第一页，每页1条，保证获取最新发布的一条数据
        Page<Announcement> page = lambdaQuery()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPublishTime)
                .page(new Page<>(1, 1));

        Announcement latest = page.getRecords().isEmpty() ? null : page.getRecords().getFirst();

        // 如果存在最新公告，转换为DTO返回；否则返回null
        if (latest != null) {
            return BeanUtils.copyBean(latest, AnnouncementVO.class);
        }

        return null;
    }

    /**
     * 获取最新公告（管理员）
     */
    @Override
    public AdminAnnouncementVO adminGetLatestAnnouncement() {

        // 查询最新公告：状态为1（已发布），按照创建时间降序，分页查询第一页，每页1条，保证获取最新发布的一条数据
        Page<Announcement> page = lambdaQuery()
                .orderByDesc(Announcement::getCreateTime)
                .page(new Page<>(1, 1));

        Announcement latest = page.getRecords().isEmpty() ? null : page.getRecords().getFirst();

        // 如果存在最新公告，转换为DTO返回；否则返回null
        // 由于hutool 的BeanUtils.copyBean()方法无法复制null值，所以这里需要手动赋值null值的字段
        if (latest != null) {
            return AdminAnnouncementVO.builder()
                    .id(latest.getId())
                    .title(latest.getTitle())
                    .content(latest.getContent())
                    .linkUrl(latest.getLinkUrl())
                    .status(latest.getStatus())
                    .publishTime(latest.getPublishTime())
                    .updateTime(latest.getUpdateTime())
                    .build();
        }

        return null;
    }

    /**
     * 添加公告
     */
    @Override
    public void addAnnouncement(AnnouncementDTO announcementDTO) {

        // 不需要特殊逻辑直接转换为实体类并保存
        Announcement announcement = BeanUtils.copyBean(announcementDTO, Announcement.class);

        // 添加一些默认值，比如状态
        announcement.setStatus(AnnouncementStatusConstant.DRAFT); // 默认状态为0（草稿）

        // 保存公告
        this.save(announcement);

    }

    /**
     * 修改公告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnouncement(UpdateAnnouncementDTO updateAnnouncementDTO) {

        // 根据ID查询公告是否存在
        Announcement announcement = this.getById(updateAnnouncementDTO.getId());
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        // 更新公告信息
        announcement.setTitle(updateAnnouncementDTO.getTitle());
        announcement.setContent(updateAnnouncementDTO.getContent());
        announcement.setLinkUrl(updateAnnouncementDTO.getLinkUrl());

        // 更新公告
        this.updateById(announcement);

    }

    /**
     * 修改公告状态
     */
    @Override
    public void changeStatus(AnnouncementStatusDTO statusDTO) {

        Announcement announcement = BeanUtils.copyBean(statusDTO, Announcement.class);

        switch (statusDTO.getStatus()) {
            case 1: // 发布
                announcement.setPublishTime(LocalDateTime.now());
                break;
            case 2: // 关闭
                announcement.setCloseTime(LocalDateTime.now());
                break;
            default:
                throw new IllegalArgumentException("无效的状态值");
        }

        this.updateById(announcement);
    }
}
