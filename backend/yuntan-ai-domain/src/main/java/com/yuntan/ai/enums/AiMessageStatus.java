package com.yuntan.ai.enums;

public enum AiMessageStatus {

    FAILED(0),  // 聊天失败
    SUCCESS(1),  // 聊天成功
    GENERATING(2),  // 聊天生成中
    REVOKED(3),    // 聊天已撤回
    CANCELLED(4);   // 聊天已取消

    private final int code;

    AiMessageStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
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