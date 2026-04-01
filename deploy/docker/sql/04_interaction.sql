
create table comment
(
    id          bigint                                 not null comment '主键，雪花ID'
        primary key,
    article_id  bigint                                 not null comment '关联文章表ID',
    user_id     bigint                                 not null comment '评论人ID（关联用户表）',
    parent_id   bigint                                 null comment '父评论ID：NULL=根评论，非空=回复某条评论',
    to_user_id  bigint                                 null comment '回复目标用户ID：NULL=根评论，非空=回复某用户',
    content     text                                   not null comment '评论内容',
    image       varchar(255) default ''                not null comment '评论附带图片',
    like_count  int          default 0                 not null comment '评论点赞数',
    status      tinyint      default 1                 not null comment '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    ip          varchar(32)  default '0.0.0.0'         not null comment '评论人IP地址',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '评论时间',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间（修改评论内容时更新）',
    deleted     tinyint      default 0                 not null comment '软删除：0-未删，1-已删'
)
    comment '评论主表（支持多级回复）' charset = utf8mb4;

create index idx_article_status_time
    on comment (article_id, status, create_time)
    comment '查询某篇文章下已审核的评论并按时间排序';

create index idx_create_time
    on comment (create_time)
    comment '按评论时间排序（全站最新评论）';

create index idx_parent_id
    on comment (parent_id)
    comment '查询某条评论的所有回复';

create index idx_user_id
    on comment (user_id)
    comment '查询某用户的所有评论';

create table comment_like
(
    id          bigint                             not null comment '主键，雪花ID'
        primary key,
    comment_id  bigint                             not null comment '关联评论表ID',
    user_id     bigint                             not null comment '点赞用户ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '点赞时间',
    constraint uk_comment_user
        unique (comment_id, user_id) comment '同一用户不能重复点赞同一条评论'
)
    comment '评论点赞关联表' charset = utf8mb4;

create index idx_comment_id
    on comment_like (comment_id)
    comment '查询某条评论的所有点赞';

create index idx_user_id
    on comment_like (user_id)
    comment '查询某用户的所有评论点赞';

create table article_like
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '关联用户表id',
    article_id  bigint                             not null comment '关联文章表id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '点赞时间',
    constraint uk_user_article
        unique (user_id, article_id) comment '防同一用户重复点赞同一文章'
)
    charset = utf8mb4;

create index idx_article_id
    on article_like (article_id)
    comment '查询文章点赞列表索引';


create table article_view
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                                null comment '关联用户表id，空=匿名用户',
    article_id  bigint                                not null comment '关联文章表id',
    ip          varchar(32) default ''                not null comment '访客IP，匿名用户去重依据',
    create_time datetime    default CURRENT_TIMESTAMP not null comment '浏览时间'
)
    charset = utf8mb4;

create index idx_article_id
    on article_view (article_id)
    comment '查询文章浏览列表索引';

create index idx_create_time
    on article_view (create_time)
    comment '按浏览时间统计索引';



create table article_collect
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '关联用户表id',
    article_id  bigint                             not null comment '关联文章表id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '收藏时间',
    constraint uk_user_article
        unique (user_id, article_id) comment '防同一用户重复收藏同一文章'
)
    charset = utf8mb4;

create index idx_article_id
    on article_collect (article_id)
    comment '查询文章收藏列表索引';

create index idx_user_id
    on article_collect (user_id)
    comment '查询用户收藏列表索引';



CREATE TABLE danmaku (
                         id           BIGINT NOT NULL COMMENT '弹幕ID（雪花ID）',
                         content      VARCHAR(50) NOT NULL COMMENT '弹幕文本内容（最多50字符）',
                         color        VARCHAR(10) DEFAULT '#FFFFFF' COMMENT '颜色值（十六进制）',
                         author       VARCHAR(50) NOT NULL COMMENT '作者名称/昵称',
                         user_id      BIGINT DEFAULT NULL COMMENT '用户ID（可选，关联用户表）',
                         create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         ip_address   VARCHAR(64) DEFAULT NULL COMMENT '发送者IP',
                         approved     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '审核状态：0-未通过/待审核，1-已通过',
                         deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删，1-已删',

                         PRIMARY KEY (id),

                         KEY idx_user_id (user_id),
                         KEY idx_created_at (create_time),
                         KEY idx_approved (approved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='弹幕表';