package com.yuntan.interaction.stat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PvDTO {


    /**
     * 页面名
     */
    @Schema(description = "页面名")
    private String page;

}
