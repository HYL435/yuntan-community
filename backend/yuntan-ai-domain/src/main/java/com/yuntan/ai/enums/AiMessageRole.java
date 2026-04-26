package com.yuntan.ai.enums;

public enum AiMessageRole {

    SYSTEM(0),
    USER(1),
    ASSISTANT(2);

    private final int code;

    AiMessageRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 获取枚举值，根据code获取对应的枚举值
    public static AiMessageRole fromCode(int code) {
        for (AiMessageRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role code: " + code);
    }
}