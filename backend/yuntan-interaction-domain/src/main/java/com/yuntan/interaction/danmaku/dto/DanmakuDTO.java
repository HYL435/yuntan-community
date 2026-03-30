package com.yuntan.interaction.danmaku.dto;

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
public class DanmakuDTO {

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
}