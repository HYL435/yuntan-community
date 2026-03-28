package com.yuntan.interaction.notification.service;

import com.yuntan.interaction.notification.manager.NotifyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyService {

    private final NotifyManager notifyManager;

    /**
     * 发送文章点赞邮件通知
     *
     * @param userId  用户ID
     */
    public void sendArticleLikeEmailNotify(Long notify,
                                           Long userId,
                                           String email,
                                           String templateCode,
                                           String subject,
                                           Map<String, Object> variables) {

        notifyManager.sendEmailNotify(notify, userId, email, templateCode, subject, variables);

    }


}
