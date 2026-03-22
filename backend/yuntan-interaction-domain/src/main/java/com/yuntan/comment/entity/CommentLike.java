package com.yuntan.comment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞记录实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "评论点赞记录")
public class CommentLike implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "点赞记录ID", example = "1")
    private Long id;

    /**
     * 关联评论ID
     */
    @Schema(
            description = "评论ID",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long commentId;

    /**
     * 点赞用户ID
     */
    @Schema(
            description = "点赞用户ID",
            example = "202",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userId;

    /**
     * 创建时间（自动填充）
     */
    @Schema(description = "点赞时间", example = "2024-01-15 11:20:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
