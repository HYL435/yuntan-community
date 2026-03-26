package com.yuntan.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.article.entity.ArticleContent;
import com.yuntan.article.mapper.ArticleContentMapper;
import com.yuntan.article.service.IArticleContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleContentServiceImpl extends ServiceImpl<ArticleContentMapper, ArticleContent> implements IArticleContentService {

    private final ArticleContentMapper articleContentMapper;


    /**
     * 根据ID查询文章内容
     */
    public Optional<String> getArticleContentById(Long articleContentId) {

        ArticleContent articleContent = this.getById(articleContentId);
        // 如果文章内容不存在，返回一个空的 Optional
        return articleContent.getContent().describeConstable();
    }

    /**
     * 添加文章内容
     */
    @Override
    public Long addArticleContent(Long id, String content) {

        ArticleContent articleContent = ArticleContent.builder()
                .id(id)
                .content(content)
                .contentType(1)
                .build();

        this.save(articleContent);

        return articleContent.getId();
    }

    /**
     * 更新文章内容
     */
    @Override
    public void updateArticleContent(Long articleContentId, String content) {

        ArticleContent articleContent = ArticleContent.builder()
                .id(articleContentId)
                .content(content)
                .build();

        this.updateById(articleContent);

    }
}
