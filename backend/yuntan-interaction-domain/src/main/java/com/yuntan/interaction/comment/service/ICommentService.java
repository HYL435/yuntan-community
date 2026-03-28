package com.yuntan.interaction.comment.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.interaction.comment.dto.admin.CommentStatusDTO;
import com.yuntan.interaction.comment.dto.front.CommentDTO;
import com.yuntan.interaction.comment.entity.Comment;
import com.yuntan.interaction.comment.query.CommentQuery;
import com.yuntan.interaction.comment.vo.admin.CommentAdminVO;
import com.yuntan.interaction.comment.vo.front.CommentVO;
import com.yuntan.infra.mysql.PageDTO;

import java.util.List;

public interface ICommentService extends IService<Comment> {

    /**
     * 添加评论
     */
    void saveComment(CommentDTO commentDTO);

    /**
     * 获取评论列表
     */
    List<CommentVO> listComments(Long articleId);

    /**
     * 获取评论数量
     */
    Integer countComments(Long articleId);

    /**
     * 后台分页查询评论列表
     */
    PageDTO<CommentAdminVO> listCommentsAdmin(CommentQuery pageQuery);

    /**
     * 修改评论状态
     */
    void updateStatusById(CommentStatusDTO commentStatusDTO);
}
