package com.yuntan.content.stat.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 站点每日统计实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "站点每日统计实体")
public class SiteStatDaily implements Serializable {

    /**
     * 主键雪花ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 统计日期
     */
    @Schema(
            description = "统计日期",
            example = "2026-04-01",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate statDate;

    /**
     * 当日页面访问量PV
     */
    @Schema(
            description = "当日页面访问量PV",
            example = "1024",
            defaultValue = "0"
    )
    private Long pvCount;

    /**
     * 当日独立访客数UV（按IP或设备去重）
     */
    @Schema(
            description = "当日独立访客数UV",
            example = "512",
            defaultValue = "0"
    )
    private Long uvCount;

    /**
     * 当日热度值
     */
    @Schema(
            description = "当日热度值",
            example = "2048",
            defaultValue = "0"
    )
    private Long hotScore;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(
            description = "创建时间",
            example = "2026-04-01 00:00:00"
    )
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(
            description = "更新时间",
            example = "2026-04-01 00:00:00"
    )
    private LocalDateTime updateTime;
}
