package com.yuntan.content.site.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.common.utils.BeanUtils;
import com.yuntan.content.site.dto.AdminSiteTimelineDTO;
import com.yuntan.content.site.dto.AdminSiteTimelineStatusDTO;
import com.yuntan.content.site.dto.SiteTimelineUpdateDTO;
import com.yuntan.content.site.entity.SiteTimeline;
import com.yuntan.content.site.mapper.SiteTimelineMapper;
import com.yuntan.content.site.query.AdminSiteTimelineQuery;
import com.yuntan.content.site.service.ISiteTimelineService;
import com.yuntan.content.site.vo.AdminSiteTimelineVO;
import com.yuntan.content.site.vo.SiteTimelineVO;
import com.yuntan.infra.mysql.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteTimelineServiceImpl extends ServiceImpl<SiteTimelineMapper, SiteTimeline> implements ISiteTimelineService {

    private final SiteTimelineMapper siteTimelineMapper;

    /**
     * 获取建站历程时间轴
     *
     * @return
     */
    @Override
    public List<SiteTimelineVO> getTimeline() {

        // 查询状态为1的建站历程时间轴，并按照sort_order字段升序排序 只查询id、event_date、title、description字段
        // 逻辑删除的记录会被自动过滤掉
        LambdaQueryChainWrapper<SiteTimeline> query = new LambdaQueryChainWrapper<>(siteTimelineMapper);
        query.eq(SiteTimeline::getStatus, 1)
                .orderByAsc(SiteTimeline::getSortOrder)  // 按照sort_order字段升序排序
                .select(SiteTimeline::getId, SiteTimeline::getEventDate, SiteTimeline::getTitle, SiteTimeline::getDescription);

        // TODO 后期做缓存优化

        // 将查询结果转换为SiteTimelineDTO列表并返回
        return query.list()
                .stream()
                .map(item -> BeanUtils.copyBean(item, SiteTimelineVO.class))
                .toList();
    }

    /**
     * 后台 获取建站历程时间轴列表
     *
     * @param query
     * @return
     */
    @Override
    public PageDTO<AdminSiteTimelineVO> listSiteTimelines(AdminSiteTimelineQuery query) {

        // 构建分页参数
        Page<SiteTimeline> page = query.toMpPage("sortOrder", true);

        // 构建查询参数
        LambdaQueryChainWrapper<SiteTimeline> wrapper = new LambdaQueryChainWrapper<>(siteTimelineMapper);

        // 动态条件：状态不为null时才添加条件
        if (query.getStatus() != null) {
            wrapper.eq(SiteTimeline::getStatus, query.getStatus());
        }

        // 动态条件：标题不为空时才添加模糊查询
        if (query.getTitle() != null && !query.getTitle().isEmpty()) {
            wrapper.like(SiteTimeline::getTitle, query.getTitle());
        }

        wrapper.orderByDesc(SiteTimeline::getSortOrder);

        // 执行分页查询并转换为VO
        Page<SiteTimeline> resultPage = wrapper.page(page);

        // 将查询结果转换为AdminSiteTimelineVO列表并返回
        return PageDTO.of(resultPage, AdminSiteTimelineVO.class);
    }

    /**
     * 后台 创建建站历程时间轴
     *
     * @param adminSiteTimelineDTO
     */
    @Override
    public void create(AdminSiteTimelineDTO adminSiteTimelineDTO) {

        // 将DTO转换为实体类
        SiteTimeline siteTimeline = BeanUtils.copyBean(adminSiteTimelineDTO, SiteTimeline.class);

        // 保存到数据库
        siteTimelineMapper.insert(siteTimeline);

    }

    /**
     * 后台 修改建站历程时间轴
     *
     * @param siteTimelineUpdateDTO
     */
    @Override
    public void updateTimeline(SiteTimelineUpdateDTO siteTimelineUpdateDTO) {

        // 将DTO转换为实体类
        SiteTimeline siteTimeline = BeanUtils.copyBean(siteTimelineUpdateDTO, SiteTimeline.class);

        // 更新到数据库
        siteTimelineMapper.updateById(siteTimeline);

    }

    /**
     * 后台 修改建站历程时间轴状态
     *
     * @param adminSiteTimelineStatusDTO
     */
    @Override
    public void updateTimelineStatus(AdminSiteTimelineStatusDTO adminSiteTimelineStatusDTO) {

        this.lambdaUpdate()
                .eq(SiteTimeline::getId, adminSiteTimelineStatusDTO.getId())
                .set(SiteTimeline::getStatus, adminSiteTimelineStatusDTO.getStatus())
                .update();

    }
}
