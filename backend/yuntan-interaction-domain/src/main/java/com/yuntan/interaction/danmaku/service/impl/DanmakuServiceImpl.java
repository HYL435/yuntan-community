package com.yuntan.interaction.danmaku.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.common.context.BaseContext;
import com.yuntan.common.utils.BeanUtils;
import com.yuntan.indentity.service.IUserService;
import com.yuntan.indentity.vo.UserVO;
import com.yuntan.infra.mysql.PageDTO;
import com.yuntan.interaction.danmaku.dto.DanmakuDTO;
import com.yuntan.interaction.danmaku.dto.DanmakuStatusDTO;
import com.yuntan.interaction.danmaku.entity.Danmaku;
import com.yuntan.interaction.danmaku.mapper.DanmakuMapper;
import com.yuntan.interaction.danmaku.query.AdminDanmakuQuery;
import com.yuntan.interaction.danmaku.service.IDanmakuService;
import com.yuntan.interaction.danmaku.vo.AdminDanmakuVO;
import com.yuntan.interaction.danmaku.vo.DanmakuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DanmakuServiceImpl extends ServiceImpl<DanmakuMapper, Danmaku> implements IDanmakuService {

    private final DanmakuMapper danmakuMapper;
    private final IUserService userService;

    /**
     * 添加弹幕
     *
     * @param danmakuDTO 弹幕参数
     */
    @Override
    public void addDanmaku(DanmakuDTO danmakuDTO) {

        // 将DTO转换为实体类
        Danmaku danmaku = BeanUtils.copyBean(danmakuDTO, Danmaku.class);

        // 添加发送人昵称，调用用户服务获取昵称
        UserVO userById = userService.getUserById(BaseContext.getUserId());

        danmaku.setAuthor(userById.getNickname());
        danmaku.setUserId(BaseContext.getUserId());
        danmaku.setIpAddress(BaseContext.getRequest().getRemoteAddr());
        danmaku.setApproved(1); // 默认审核通过，后续可以添加审核流程

        // 保存弹幕到数据库
        this.save(danmaku);
    }

    /**
     * 获取弹幕列表
     *
     * @return 弹幕列表
     */
    @Override
    public List<DanmakuVO> getDanmakuList() {

        // TODO 后续改为redis缓存，提升性能

        // 查询数据库
        List<Danmaku> danmakus = danmakuMapper.selectList(null);

        // 将实体类列表转换为VO列表
        return BeanUtils.copyList(danmakus, DanmakuVO.class);
    }

    /**
     * 获取后台弹幕列表
     *
     * @param pageQuery 分页参数
     * @return 弹幕列表
     */
    @Override
    public PageDTO<AdminDanmakuVO> listDanmakuAdmin(AdminDanmakuQuery pageQuery) {

        // 获取page对象 - 使用默认按创建时间倒序排序
        Page<Danmaku> mpPage = pageQuery.toMpPageDefaultSortByCreateTimeDesc();

        // 设置查询条件，不再添加额外的orderByDesc
        LambdaQueryChainWrapper<Danmaku> queryWrapper = new LambdaQueryChainWrapper<>(this.baseMapper)
                .eq(pageQuery.getApproved() != null, Danmaku::getApproved, pageQuery.getApproved())
                .eq(StringUtils.hasText(pageQuery.getAuthor()), Danmaku::getAuthor, pageQuery.getAuthor());

        // 执行分页查询
        Page<Danmaku> danmakuPage = queryWrapper.page(mpPage);

        // 将实体类列表转换为VO列表并返回分页结果
        return PageDTO.of(danmakuPage, AdminDanmakuVO.class);
    }


    /**
     * 修改弹幕状态
     *
     * @param danmakuStatusDTO 弹幕状态参数
     */
    @Override
    public void updateStatusById(DanmakuStatusDTO danmakuStatusDTO) {

        // 将DTO转换为实体类
        Danmaku danmaku = BeanUtils.copyBean(danmakuStatusDTO, Danmaku.class);

        // 修改弹幕状态
        this.updateById(danmaku);

    }
}
