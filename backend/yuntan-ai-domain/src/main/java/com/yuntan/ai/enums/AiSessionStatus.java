package com.yuntan.ai.enums;

import lombok.Getter;

@Getter
public enum AiSessionStatus {

    CLOSED(0), // 关闭
    NORMAL(1); // 正常

    private final int code;

    AiSessionStatus(int code) {
        this.code = code;
    }

    public static AiSessionStatus fromCode(int code) {
        for (AiSessionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}