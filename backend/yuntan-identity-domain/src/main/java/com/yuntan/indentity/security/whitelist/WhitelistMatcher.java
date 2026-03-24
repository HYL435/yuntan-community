package com.yuntan.indentity.security.whitelist;

import com.yuntan.indentity.security.properties.SecurityWhiteListProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class WhitelistMatcher {

    private final SecurityWhiteListProperties whiteListProperties;
    // AntPathMatcher 用于路径匹配，支持通配符
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 判断请求路径是否在白名单中
     *
     * @param requestPath 请求路径
     * @return 是否在白名单中
     */
    public boolean isWhitelisted(String requestPath) {
        return whiteListProperties.getPaths()
                .stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

}
