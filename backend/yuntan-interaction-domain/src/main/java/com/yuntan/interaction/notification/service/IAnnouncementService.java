package com.yuntan.interaction.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.interaction.notification.dto.AnnouncementDTO;
import com.yuntan.interaction.notification.dto.AnnouncementStatusDTO;
import com.yuntan.interaction.notification.dto.UpdateAnnouncementDTO;
import com.yuntan.interaction.notification.entity.Announcement;
import com.yuntan.interaction.notification.vo.AdminAnnouncementVO;
import com.yuntan.interaction.notification.vo.AnnouncementVO;

public interface IAnnouncementService extends IService<Announcement> {

    /**
     * 获取最新公告
     *
     * @return
     */
    AnnouncementVO getLatestAnnouncement();

    /**
     * 管理员获取最新公告
     *
     * @return
     */
    AdminAnnouncementVO adminGetLatestAnnouncement();

    /**
     * 添加公告
     *
     * @param announcementDTO
     */
    void addAnnouncement(AnnouncementDTO announcementDTO);

    /**
     * 更新公告
     *
     * @param updateAnnouncementDTO
     */
    void updateAnnouncement(UpdateAnnouncementDTO updateAnnouncementDTO);

    /**
     * 修改公告状态
     *
     * @param statusDTO
     */
    void changeStatus(AnnouncementStatusDTO statusDTO);
}
