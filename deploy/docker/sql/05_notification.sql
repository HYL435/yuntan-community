
create table user_notify
(
    id            bigint                             not null comment '主键雪花ID'
        primary key,
    user_id       bigint                             not null comment '接收通知用户ID',
    template_code varchar(50)                        not null comment '关联消息模板编码',
    msg_content   text                               not null comment '实际发送的消息内容（替换占位符后）',
    msg_type      tinyint                            not null comment '消息类型：1系统通知，2互动通知，3邮件，4短信',
    send_status   tinyint  default 0                 not null comment '发送状态：0-待发送，1-发送成功，2-发送失败',
    send_time     datetime                           null comment '发送时间',
    read_status   tinyint  default 0                 not null comment '阅读状态：0-未读，1-已读',
    read_time     datetime                           null comment '阅读时间',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户通知记录表' charset = utf8mb4;

create index idx_read_status
    on user_notify (read_status)
    comment '按阅读状态查询索引';

create index idx_send_status
    on user_notify (send_status)
    comment '按发送状态查询索引';

create index idx_user_id
    on user_notify (user_id)
    comment '按用户查询通知索引';

create table msg_template
(
    id            bigint                             not null comment '主键雪花ID'
        primary key,
    template_code varchar(50)                        not null comment '模版唯一编码，如ARTICLE_LIKE',
    template_name varchar(100)                       not null comment '模版名称',
    msg_content   text                               not null comment '模版内容，支持占位符：{userNick}、{articleTitle}',
    msg_type      tinyint                            not null comment '消息类型：1系统通知，2互动通知，3邮件，4短信',
    send_channel  tinyint                            not null comment '发送渠道：1-站内信，2-邮件，3-短信，4-多渠道',
    status        tinyint  default 1                 not null comment '状态：0停用，1启用',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_template_code
        unique (template_code) comment '模板编码唯一'
)
    charset = utf8mb4;

create index idx_msg_type
    on msg_template (msg_type)
    comment '按消息类型查询索引';

create index idx_status
    on msg_template (status)
    comment '按模版状态查询索引';



CREATE TABLE announcement
(
    id            BIGINT                               NOT NULL COMMENT '主键雪花ID'
        PRIMARY KEY,

    title         VARCHAR(60)                          NOT NULL COMMENT '公告标题',

    content       VARCHAR(500)                         NOT NULL COMMENT '公告内容',

    link_url      VARCHAR(255)                         NULL COMMENT '跳转链接，可为空',

    status        TINYINT    DEFAULT 0                NOT NULL COMMENT '状态：0-草稿，1-已发布，2-已关闭',

    sort          BIGINT     DEFAULT 0                NOT NULL COMMENT '排序值，越大越靠前',

    publish_time  DATETIME                             NULL COMMENT '发布时间',

    close_time    DATETIME                             NULL COMMENT '关闭时间',

    deleted       TINYINT    DEFAULT 0                NOT NULL COMMENT '逻辑删除：0-未删，1-已删',

    create_time   DATETIME   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',

    update_time   DATETIME   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
)
    COMMENT '网站公告表'
    CHARSET = utf8mb4;

CREATE INDEX idx_announcement_status
    ON announcement (status)
    COMMENT '按状态查询索引';

CREATE INDEX idx_announcement_sort
    ON announcement (sort)
    COMMENT '按排序查询索引';

CREATE INDEX idx_announcement_publish_time
    ON announcement (publish_time)
    COMMENT '按发布时间查询索引';