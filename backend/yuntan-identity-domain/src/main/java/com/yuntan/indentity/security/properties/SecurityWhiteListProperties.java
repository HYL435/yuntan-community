package com.yuntan.indentity.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "security.whitelist")
public class SecurityWhiteListProperties {

    private List<String> paths = new ArrayList<>();

}
