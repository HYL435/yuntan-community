package com.yuntan.interaction.notification.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "公告实体")
public class AnnouncementStatusDTO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "公告ID", example = "1")
    private Long id;

    /**
     * 状态：0-草稿，1-已发布，2-已关闭
     */
    @Schema(
            description = "状态：0-草稿，1-已发布，2-已关闭",
            example = "1",
            allowableValues = {"0", "1", "2"},
            defaultValue = "0"
    )
    private Integer status;

}
