package com.yuntan.common.exception;

public class AiRateLimitException extends RuntimeException {

    public AiRateLimitException(String message) {
        super(message);
    }
}
