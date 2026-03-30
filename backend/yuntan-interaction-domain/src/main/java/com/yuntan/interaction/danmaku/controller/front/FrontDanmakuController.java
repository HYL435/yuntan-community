package com.yuntan.interaction.danmaku.controller.front;

import com.yuntan.common.domain.Result;
import com.yuntan.interaction.danmaku.dto.DanmakuDTO;
import com.yuntan.interaction.danmaku.service.IDanmakuService;
import com.yuntan.interaction.danmaku.vo.DanmakuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/front/danmaku")
@Tag(name = "前台-弹幕接口")
public class FrontDanmakuController {

   private final IDanmakuService danmakuService;


   /**
    * 添加弹幕
    */
   @PostMapping
   @Operation(summary = "添加弹幕")
   public Result<Void> addDanmaku(@RequestBody DanmakuDTO danmakuDTO) {

       log.info("添加弹幕，弹幕内容：{}", danmakuDTO.getContent());

      danmakuService.addDanmaku(danmakuDTO);

      return Result.ok();
   }

    /**
     * 获取弹幕列表
     */
    @GetMapping
    @Operation(summary = "获取弹幕列表")
    public Result<List<DanmakuVO>> list() {

        log.info("获取弹幕列表");

        List<DanmakuVO> list = danmakuService.getDanmakuList();

        return Result.ok(list);
    }



}
