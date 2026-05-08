package com.yuntan.ai.enums;

import lombok.Getter;

@Getter
public enum AiMessageStatus {

    FAILED(0, "失败"),  // 聊天失败
    SUCCESS(1, "成功"),  // 聊天成功
    GENERATING(2, "处理中"),  // 聊天生成中
    REVOKED(3, "已撤回"),    // 聊天已撤回
    CANCELLED(4, "已取消");   // 聊天已取消

    private final int code;
    private final String desc;

    AiMessageStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 获取枚举值，根据code获取对应的枚举值
    public static AiMessageStatus fromCode(int code) {
        for (AiMessageStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}