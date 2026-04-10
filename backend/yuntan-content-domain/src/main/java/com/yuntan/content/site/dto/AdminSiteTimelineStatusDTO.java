package com.yuntan.content.site.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建站历程时间轴实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("site_timeline")
@Schema(description = "建站历程时间轴实体")
public class AdminSiteTimelineStatusDTO {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID", example = "1910000000000000001")
    private Long id;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}