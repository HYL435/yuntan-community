package com.yuntan.indentity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuntan.indentity.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
