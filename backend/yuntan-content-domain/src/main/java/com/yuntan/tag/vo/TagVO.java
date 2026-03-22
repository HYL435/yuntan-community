package com.yuntan.tag.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@Builder
@Schema(description = "标签实体")
public class TagVO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "标签ID", example = "1")
    private Long id;

    /**
     * 标签名称
     */
    @Schema(
            description = "标签名称",
            example = "Java",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 50
    )
    private String tagName;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(
            description = "状态：0-禁用，1-启用",
            example = "1",
            allowableValues = {"0", "1"},
            defaultValue = "1"
    )
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(
            description = "创建时间",
            example = "2024-01-01 00:00:00"
    )
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(
            description = "最后更新时间",
            example = "2024-01-15 09:00:00"
    )
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
