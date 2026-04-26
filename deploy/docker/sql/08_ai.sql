
CREATE TABLE ai_chat_session (
                                 id               BIGINT NOT NULL COMMENT '会话ID',
                                 user_id          BIGINT NOT NULL COMMENT '用户ID',

                                 title            VARCHAR(200) DEFAULT NULL COMMENT '会话标题',

                                 next_sequence_no INT NOT NULL DEFAULT 1 COMMENT '下一条消息序号',

                                 status           TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已关闭 1-正常 2-已归档',
                                 deleted          TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',

                                 create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                 PRIMARY KEY (id),

                                 KEY idx_user_id (user_id),
                                 KEY idx_status (status),
                                 KEY idx_deleted (deleted),
                                 KEY idx_user_deleted (user_id, deleted),
                                 KEY idx_user_status_deleted (user_id, status, deleted),
                                 KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话表';



CREATE TABLE ai_chat_message (
                                 id                BIGINT NOT NULL COMMENT '消息ID',
                                 session_id        BIGINT NOT NULL COMMENT '会话ID',
                                 user_id           BIGINT NOT NULL COMMENT '用户ID',

                                 role              TINYINT NOT NULL COMMENT '角色：0-system 1-user 2-assistant',
                                 sequence_no       INT NOT NULL COMMENT '会话内消息顺序号，从1递增',

                                 content           LONGTEXT DEFAULT NULL COMMENT '消息内容；流式生成中可为空，完成后回填',

                                 provider          VARCHAR(50) DEFAULT NULL COMMENT '模型提供商，如 deepseek/qwen',
                                 model        VARCHAR(100) DEFAULT NULL COMMENT '模型名称',

                                 prompt_tokens     INT DEFAULT NULL COMMENT '输入tokens',
                                 completion_tokens INT DEFAULT NULL COMMENT '输出tokens',
                                 total_tokens      INT DEFAULT NULL COMMENT '总tokens',

                                 finish_reason     VARCHAR(50) DEFAULT NULL COMMENT '结束原因：stop/length/error/cancelled',

                                 status            TINYINT NOT NULL DEFAULT 2 COMMENT '状态：0-失败 1-成功 2-处理中 3-已撤回 4-已取消',
                                 error_message     VARCHAR(500) DEFAULT NULL COMMENT '失败原因',

                                 trace_id          VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
                                 request_id        VARCHAR(64) NOT NULL COMMENT '请求ID/幂等ID，由前端生成',

                                 deleted           TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',

                                 create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                 PRIMARY KEY (id),

                                 UNIQUE KEY uk_session_sequence (session_id, sequence_no),
                                 UNIQUE KEY uk_user_request_role (user_id, request_id, role),

                                 KEY idx_session_id (session_id),
                                 KEY idx_user_id (user_id),
                                 KEY idx_session_deleted_seq (session_id, deleted, sequence_no),
                                 KEY idx_session_status_deleted (session_id, status, deleted),
                                 KEY idx_trace_id (trace_id),
                                 KEY idx_request_id (request_id),
                                 KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表';