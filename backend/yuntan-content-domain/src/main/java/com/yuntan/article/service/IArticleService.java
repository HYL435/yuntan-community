package com.yuntan.article.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.article.dto.ArticleSaveDTO;
import com.yuntan.article.dto.ArticleStatusDTO;
import com.yuntan.article.entity.Article;
import com.yuntan.article.query.ArticleExhibitQuery;
import com.yuntan.article.query.ArticleManageQuery;
import com.yuntan.article.vo.ArticleAdminVO;
import com.yuntan.article.vo.ArticleDetailVO;
import com.yuntan.article.vo.ArticleExhibitFrontVO;
import com.yuntan.article.vo.ArticleFrontVO;
import com.yuntan.dto.ArticleInfoDTO;
import com.yuntan.infra.mysql.PageDTO;
import com.yuntan.infra.mysql.PageQuery;

public interface IArticleService extends IService<Article> {

    /**
     * 根据文章ID获取文章信息
     */
    ArticleFrontVO getArticleById(Long id);

    /**
     * 分页查询文章列表
     */
    PageDTO<ArticleExhibitFrontVO> pageExhibit(PageQuery pageQuery);

    /**
     * 根据分类或标签分页查询文章列表
     */
    PageDTO<ArticleExhibitFrontVO> pageExhibitByCategoryOrTags(ArticleExhibitQuery articleExhibitQuery);

    /**
     * 分页查询文章管理列表
     */
    PageDTO<ArticleAdminVO> pageManage(ArticleManageQuery articleManageQuery);

    /**
     * 文章置顶
     */
    void articleTop(Long id, Integer top);

    /**
     * 获取文章详情
     */
    ArticleDetailVO getArticleDetail(Long id);

    /**
     * 保存文章
     */
    void saveArticle(ArticleSaveDTO articleSaveDTO);


    /**
     * 更新文章状态
     */
    void updateArticleStatus(ArticleStatusDTO articleStatusDTO);

    /**
     * 根据文章ID获取文章信息
     */
    ArticleInfoDTO getArticleInfoById(Long id);
}
