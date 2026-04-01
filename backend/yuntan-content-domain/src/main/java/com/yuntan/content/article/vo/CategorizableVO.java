package com.yuntan.content.article.vo;

import java.util.List;

public interface CategorizableVO {
    Long getId();
    void setCategory(String category);
    void setTags(List<String> tags);
}