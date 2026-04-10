package com.yuntan.content.site.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 建站历程时间轴实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("site_timeline")
@Schema(description = "建站历程时间轴实体")
public class SiteTimelineUpdateDTO {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID", example = "1910000000000000001")
    private Long id;

    @Schema(description = "时间节点日期（精确到日）", example = "2026-04-10")
    private LocalDate eventDate;

    @Schema(description = "节点标题", example = "性能与细节打磨")
    private String title;

    @Schema(description = "节点描述", example = "优化滚动与渲染开销，完善页面动画节奏与时间信息展示。")
    private String description;

    @Schema(description = "排序值（越小越靠前）", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}