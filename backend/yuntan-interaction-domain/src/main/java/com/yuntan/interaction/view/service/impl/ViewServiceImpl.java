package com.yuntan.interaction.view.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.interaction.view.entity.ArticleView;
import com.yuntan.interaction.view.manager.ViewManager;
import com.yuntan.interaction.view.mapper.ViewMapper;
import com.yuntan.interaction.view.service.IViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewServiceImpl extends ServiceImpl<ViewMapper, ArticleView> implements IViewService {

    private final ViewManager viewManager;

    /**
     * 浏览文章
     */
    @Override
    public void view(Long articleId) {

        viewManager.viewEvent(articleId);
    }
}
