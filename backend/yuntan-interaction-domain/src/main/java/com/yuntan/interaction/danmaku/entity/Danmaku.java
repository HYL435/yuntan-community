package com.yuntan.interaction.danmaku.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 弹幕实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "弹幕实体")
public class Danmaku implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "弹幕ID", example = "1")
    private Long id;

    /**
     * 弹幕内容
     */
    @Schema(
            description = "弹幕文本内容（最多50字符）",
            example = "哈哈哈哈",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 50
    )
    private String content;

    /**
     * 弹幕颜色
     */
    @Schema(
            description = "颜色值（十六进制）",
            example = "#FFFFFF"
    )
    private String color;

    /**
     * 作者昵称
     */
    @Schema(
            description = "作者名称/昵称",
            example = "小明",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String author;

    /**
     * 用户ID（可选）
     */
    @Schema(
            description = "用户ID（可选）",
            example = "2"
    )
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(
            description = "创建时间",
            example = "2024-01-15 10:30:00"
    )
    private LocalDateTime createTime;

    /**
     * IP地址
     */
    @Schema(
            description = "发送者IP",
            example = "192.168.1.1"
    )
    private String ipAddress;

    /**
     * 审核状态
     */
    @Schema(
            description = "审核状态：0-未通过/待审核，1-已通过",
            example = "1"
    )
    private Integer approved;

    /**
     * 逻辑删除
     */
    @TableLogic
    @Schema(
            description = "逻辑删除：0-未删，1-已删",
            example = "0"
    )
    private Integer deleted;
}