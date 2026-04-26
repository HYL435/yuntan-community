package com.yuntan.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SequencePair {

    /**
     * 用户消息序列号
     */
    private Integer userSeq;

    /**
     * 助手消息序列号
     */
    private Integer assistantSeq;

}
