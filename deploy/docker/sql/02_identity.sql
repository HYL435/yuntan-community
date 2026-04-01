create table user
(
    id              bigint                                 not null comment '唯一主键标识，雪花ID'
        primary key,
    username        varchar(32)                            not null comment '用户名',
    password        varchar(64)                            not null comment '密码，BCrypt加密后存储',
    nickname        varchar(32)                            null comment '博客展示名称，区别于用户名',
    image           varchar(255) default ''                not null comment '用户头像URL',
    email           varchar(45)                            not null comment '邮箱，找回密码+接收通知',
    phone           varchar(11)                            null comment '手机号',
    intro           varchar(200) default ''                null comment '个人简介',
    role            tinyint      default 2                 not null comment '权限：0博主，1管理员，2普通访客（默认为2普通访客）',
    status          tinyint      default 1                 not null comment '账号状态（1-正常，2-禁用）',
    last_login_time datetime                               null comment '最后登录时间',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '注册时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted         tinyint      default 0                 not null comment '软删除（1-已删，0-未删）',
    constraint uk_email
        unique (email) comment '邮箱唯一，加速邮箱登录/找回密码',
    constraint uk_nickname
        unique (nickname) comment '昵称唯一，加速前台按昵称搜索',
    constraint uk_phone
        unique (phone) comment '手机号唯一，加速按手机号查询',
    constraint uk_username
        unique (username) comment '用户名唯一，加速登录/按用户名查询'
)
    charset = utf8mb4;

create index idx_role
    on user (role)
    comment '按角色筛选（博主/管理员），提速后台查询';

create index idx_status
    on user (status)
    comment '按账号状态筛选（正常/禁用），提速登录校验/后台查询';