package com.yuntan.interaction.danmaku.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.infra.mysql.PageDTO;
import com.yuntan.interaction.danmaku.dto.DanmakuDTO;
import com.yuntan.interaction.danmaku.dto.DanmakuStatusDTO;
import com.yuntan.interaction.danmaku.entity.Danmaku;
import com.yuntan.interaction.danmaku.query.AdminDanmakuQuery;
import com.yuntan.interaction.danmaku.vo.AdminDanmakuVO;
import com.yuntan.interaction.danmaku.vo.DanmakuVO;

import java.util.List;

public interface IDanmakuService extends IService<Danmaku> {

    /**
     * 添加弹幕
     */
    void addDanmaku(DanmakuDTO danmakuDTO);

    /**
     * 获取弹幕列表
     */
    List<DanmakuVO> getDanmakuList();

    /**
     * 获取弹幕列表（后台）
     */
    PageDTO<AdminDanmakuVO> listDanmakuAdmin(AdminDanmakuQuery pageQuery);

    /**
     * 修改弹幕状态
     */
    void updateStatusById(DanmakuStatusDTO danmakuStatusDTO);
}
