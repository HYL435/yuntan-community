package com.yuntan.interaction.danmaku.query;

import com.yuntan.infra.mysql.PageQuery;
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
public class AdminDanmakuQuery extends PageQuery {

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
     * 审核状态
     */
    @Schema(
            description = "审核状态：0-未通过/待审核，1-已通过",
            example = "1"
    )
    private Integer approved;
}