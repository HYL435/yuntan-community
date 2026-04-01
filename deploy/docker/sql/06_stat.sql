
CREATE TABLE site_stat_daily
(
    id           BIGINT                             NOT NULL COMMENT '主键雪花ID'
        PRIMARY KEY,

    stat_date    DATE                               NOT NULL COMMENT '统计日期',

    pv_count     BIGINT   DEFAULT 0                 NOT NULL COMMENT '当日页面访问量PV',

    uv_count     BIGINT   DEFAULT 0                 NOT NULL COMMENT '当日独立访客数UV（按IP或设备去重）',

    hot_score    BIGINT   DEFAULT 0                 NOT NULL COMMENT '当日热度值',

    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',

    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT uk_stat_date UNIQUE (stat_date)
)
    COMMENT '站点每日统计表'
    CHARSET = utf8mb4;

CREATE INDEX idx_stat_date
    ON site_stat_daily (stat_date)
    COMMENT '按统计日期查询索引';
