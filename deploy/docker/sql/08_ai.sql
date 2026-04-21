CREATE TABLE ai_chat_session (
                                 id            BIGINT NOT NULL COMMENT '会话ID',
                                 user_id       BIGINT NOT NULL COMMENT '用户ID',
                                 title         VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
                                 provider      VARCHAR(50)  DEFAULT NULL COMMENT '默认模型提供商',
                                 status        TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-关闭',
                                 create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (id),
                                 KEY idx_user_id (user_id),
                                 KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话表';

CREATE TABLE ai_chat_message (
                                 id              BIGINT NOT NULL COMMENT '消息ID',
                                 session_id      BIGINT NOT NULL COMMENT '会话ID',
                                 user_id         BIGINT NOT NULL COMMENT '用户ID',
                                 role            VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
                                 content         TEXT NOT NULL COMMENT '消息内容',
                                 provider        VARCHAR(50) DEFAULT NULL COMMENT '模型提供商',
                                 model_name      VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
                                 status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-成功 0-失败',
                                 error_message   VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
                                 create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (id),
                                 KEY idx_session_id (session_id),
                                 KEY idx_user_id (user_id),
                                 KEY idx_role (role),
                                 KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表';