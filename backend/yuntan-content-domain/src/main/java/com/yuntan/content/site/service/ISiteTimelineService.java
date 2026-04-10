package com.yuntan.content.site.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.content.site.dto.AdminSiteTimelineDTO;
import com.yuntan.content.site.dto.AdminSiteTimelineStatusDTO;
import com.yuntan.content.site.dto.SiteTimelineUpdateDTO;
import com.yuntan.content.site.entity.SiteTimeline;
import com.yuntan.content.site.query.AdminSiteTimelineQuery;
import com.yuntan.content.site.vo.AdminSiteTimelineVO;
import com.yuntan.content.site.vo.SiteTimelineVO;
import com.yuntan.infra.mysql.PageDTO;

import java.util.List;

public interface ISiteTimelineService extends IService<SiteTimeline> {


    /**
     * 获取建站历程时间轴
     *
     * @return
     */
    List<SiteTimelineVO> getTimeline();

    /**
     * 后台获取建站历程时间轴列表
     *
     * @param query
     * @return
     */
    PageDTO<AdminSiteTimelineVO> listSiteTimelines(AdminSiteTimelineQuery query);

    /**
     * 后台创建建站历程时间轴
     *
     * @param adminSiteTimelineDTO
     */
    void create(AdminSiteTimelineDTO adminSiteTimelineDTO);

    /**
     * 后台更新建站历程时间轴
     *
     * @param adminSiteTimelineDTO
     */
    void updateTimeline(SiteTimelineUpdateDTO adminSiteTimelineDTO);

    /**
     * 后台更新建站历程时间轴状态
     *
     * @param adminSiteTimelineStatusDTO
     */
    void updateTimelineStatus(AdminSiteTimelineStatusDTO adminSiteTimelineStatusDTO);
}
