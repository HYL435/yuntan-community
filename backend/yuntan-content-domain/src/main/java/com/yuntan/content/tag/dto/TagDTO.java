package com.yuntan.content.tag.dto;

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
public class TagDTO implements Serializable {

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

}
