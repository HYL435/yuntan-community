package com.yuntan.content.tag.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 标签实体类
 */
@Data
@Builder
@Schema(description = "标签实体")
public class TagUpdateDTO implements Serializable {

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
}
