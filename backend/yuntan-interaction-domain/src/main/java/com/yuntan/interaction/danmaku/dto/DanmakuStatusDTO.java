package com.yuntan.interaction.danmaku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 弹幕实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "弹幕实体")
public class DanmakuStatusDTO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "弹幕ID", example = "1")
    private Long id;

    /**
     * 审核状态
     */
    @Schema(
            description = "审核状态：0-未通过/待审核，1-已通过",
            example = "1"
    )
    private Integer approved;
}