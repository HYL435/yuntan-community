
create table article
(
    id            bigint                                 not null comment '主键ID雪花'
        primary key,
    title         varchar(128)                           null comment '文章标题',
    summary       varchar(512) default ''                null comment '文章摘要，列表页展示',
    cover_img     varchar(255) default ''                null comment '文章封面图URL',
    article_content_id      varchar(24)                            null comment '文章内容的id',
    author_id     bigint                                 not null comment '关联用户表ID（作者）',
    status        tinyint      default 0                 not null comment '文章状态：0-草稿，1-已发布，2-私密（默认草稿）',
    is_top        tinyint      default 0                 not null comment '是否置顶：0-否，1-是（默认0否）',
    is_original   tinyint      default 1                 not null comment '是否原创：0-转载，1-原创（默认1原创）',
    view_count    int          default 0                 not null comment '文章浏览量（缓存同步）',
    like_count    int          default 0                 not null comment '文章点赞数（缓存同步）',
    collect_count int          default 0                 not null comment '文章收藏数（缓存同步）',
    comment_count int          default 0                 not null comment '文章评论数（缓存同步）',
    keywords      varchar(128) default ''                null comment '文章关键词（SEO用）',
    publish_time  datetime                               null comment '发布时间（草稿转发布时填充，允许为空）',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted       tinyint      default 0                 not null comment '软删除：0-未删，1-已删'
)
    charset = utf8mb4;

create index idx_author_id
    on article (author_id)
    comment '按作者查询文章索引';

create index idx_is_top
    on article (is_top)
    comment '按置顶状态查询索引';

create index idx_publish_time
    on article (publish_time)
    comment '按发布时间排序索引';

create index idx_status
    on article (status)
    comment '按文章状态查询索引';


CREATE TABLE article_content
(
    id              BIGINT                               NOT NULL COMMENT '正文ID（雪花ID）',
    article_id      BIGINT                               NOT NULL COMMENT '关联文章ID',
    content         LONGTEXT                             NOT NULL COMMENT '文章正文内容',
    content_type    TINYINT      DEFAULT 1               NOT NULL COMMENT '正文类型：1-Markdown，2-HTML',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_article_id (article_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='文章正文表';


create table category
(
    id            bigint                             not null comment '主键雪花ID'
        primary key,
    category_name varchar(32)                        not null comment '分类名称',
    sort          bigint                             not null comment '排序权重，越大越靠前',
    status        tinyint  default 1                 not null comment '状态：0-禁用，1-启用',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted       tinyint  default 0                 not null comment '软删除：0-未删，1-已删',
    constraint uk_category_name
        unique (category_name) comment '分类名称唯一'
)
    comment '文章分类表' charset = utf8mb4;

create table tag
(
    id          bigint                             not null comment '主键雪花ID'
        primary key,
    tag_name    varchar(32)                        not null comment '标签名称',
    status      tinyint  default 1                 not null comment '状态：1-启用，0-禁用',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 not null comment '软删除：0-未删，1-已删',
    constraint uk_tag_name
        unique (tag_name) comment '标签名称唯一'
)
    comment '文章标签表' charset = utf8mb4;




create table article_category
(
    id          bigint                             not null comment '主键，雪花ID'
        primary key,
    article_id  bigint                             not null comment '关联文章表ID（唯一）',
    category_id bigint                             not null comment '关联分类表ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '关联创建时间',
    constraint uk_article_id
        unique (article_id) comment '一篇文章只能关联一个分类'
)
    comment '文章-分类关联表（仅支持单分类）' charset = utf8mb4;

create index idx_category_id
    on article_category (category_id)
    comment '查询某个分类下的所有文章';



create table article_tag
(
    id          bigint                             not null comment '主键雪花ID'
        primary key,
    article_id  bigint                             not null comment '关联文章表ID',
    tag_id      bigint                             not null comment '关联标签表ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '关联创建时间（绑定标签的时间）',
    constraint uk_article_tag
        unique (article_id, tag_id) comment '防重复关联'
)
    comment '文章-标签关联表' charset = utf8mb4;

create index idx_article_id
    on article_tag (article_id)
    comment '查询文章的所有标签';

create index idx_create_time
    on article_tag (create_time)
    comment '按绑定时间筛选（可选，非刚需）';

create index idx_tag_id
    on article_tag (tag_id)
    comment '查询标签下的所有文章';

