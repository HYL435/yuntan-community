package com.yuntan.interaction.notification.template;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotifyTemplateRender {

    /**
     * 渲染模板
     */
    public String render(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }

        String result = template;

        // 如果没有变量，则直接返回模板
        if (variables == null || variables.isEmpty()) {
            return result;
        }

        // 遍历变量，替换模板中的占位符
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            // 构建占位符，例如 {username}
            String placeholder = "{" + entry.getKey() + "}";
            //  将占位符替换为变量值，如果变量值为 null，则替换为空字符串
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            // 替换模板中的占位符
            result = result.replace(placeholder, value);
        }

        return result;
    }
}