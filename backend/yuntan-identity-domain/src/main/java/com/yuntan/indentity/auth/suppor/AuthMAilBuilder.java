package com.yuntan.indentity.auth.suppor;

import org.springframework.stereotype.Component;

@Component
public class AuthMAilBuilder {

    public String buildWelcomeSubject() {
        return "欢迎加入 - 雲 壇";
    }

    public String buildWelcomeContent(String username) {
        return """
                你好，%s！欢迎加入雲壇！我们很高兴你成为我们社区的一员。
                祝你使用愉快！
                
                如果有任何问题或需要帮助，请随时联系我们的支持团队。
                """.formatted( username);
    }

    public String buildResetPasswordSubject() {
        return "雲 壇 - 找回密码";
    }

    public String buildResetPasswordContent(String username, String code) {
        return """
                你好，%s！
                
                你正在申请找回密码，请输入下面的验证码完成验证：
                %s
                
                如果你没有请求密码重置，请忽略此邮件。
                
                云 坛
                """.formatted(username, code);
    }

}
