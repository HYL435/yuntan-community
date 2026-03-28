package com.yuntan.interaction.view.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.interaction.view.entity.ArticleView;

public interface IViewService extends IService<ArticleView> {

    /**
     * 浏览量
     */
    void view(Long articleId);
}
