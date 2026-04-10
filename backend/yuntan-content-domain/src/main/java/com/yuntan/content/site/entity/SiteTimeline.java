package com.yuntan.content.site.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 建站历程时间轴实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("site_timeline")
@Schema(description = "建站历程时间轴实体")
public class SiteTimeline implements Serializable {

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

    @TableLogic
    @Schema(description = "软删除：0-未删，1-已删", example = "0")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2026-04-10 12:00:00")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2026-04-10 12:00:00")
    private LocalDateTime updateTime;
}