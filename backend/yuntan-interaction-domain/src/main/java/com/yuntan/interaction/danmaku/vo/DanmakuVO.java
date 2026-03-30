package com.yuntan.interaction.danmaku.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 弹幕实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "弹幕实体")
public class DanmakuVO implements Serializable {

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
}