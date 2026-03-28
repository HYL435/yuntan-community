package com.yuntan.interaction.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuntan.interaction.notification.entity.MsgTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MsgTemplateMapper extends BaseMapper<MsgTemplate> {

    /**
     * 根据模版编码查询模版信息
     *
     * @param templateCode 模版编码
     * @return 模版信息
     */
    @Select("select * from msg_template where template_code = #{templateCode} limit 1")
    MsgTemplate selectByCode(String templateCode);

}
