package com.yuntan.interaction.notification.manager;


import com.yuntan.infra.mail.service.MailService;
import com.yuntan.interaction.notification.entity.MsgTemplate;
import com.yuntan.interaction.notification.entity.UserNotify;
import com.yuntan.interaction.notification.enums.NotifyReadStatusEnum;
import com.yuntan.interaction.notification.enums.NotifySendChannelEnum;
import com.yuntan.interaction.notification.enums.NotifySendStatusEnum;
import com.yuntan.interaction.notification.mapper.MsgTemplateMapper;
import com.yuntan.interaction.notification.mapper.UserNotifyMapper;
import com.yuntan.interaction.notification.template.NotifyTemplateRender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.yuntan.common.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotifyManager {

    private final MsgTemplateMapper msgTemplateMapper;
    private final UserNotifyMapper userNotifyMapper;
    private final NotifyTemplateRender templateRender;
    private final MailService mailService;

    /**
     * 发送邮件通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailNotify(Long notifyId,
                                Long userId,
                                String email,
                                String templateCode,
                                String subject,
                                Map<String, Object> variables) {

        // 查询消息模板
        MsgTemplate template = msgTemplateMapper.selectByCode(templateCode);
        if (template == null) {
            throw new BusinessException("消息模板不存在或未启用");
        }

        // 渲染模板内容
        String content = templateRender.render(template.getMsgContent(), variables);

        UserNotify notify = new UserNotify();
        notify.setId(notifyId);
        notify.setUserId(userId);
        notify.setTemplateCode(templateCode);
        notify.setMsgContent(content);
        notify.setMsgType(template.getMsgType());
        notify.setSendStatus(NotifySendStatusEnum.PENDING);
        notify.setReadStatus(NotifyReadStatusEnum.UNREAD);
        notify.setSendTime(null);
        notify.setReadTime(null);

        // 插入数据库记录发送日志
        userNotifyMapper.insert(notify);

        try {
            // 发送邮件，邮件模板和发送渠道在数据库中配置，支持多渠道发送
            if (template.getSendChannel() == NotifySendChannelEnum.EMAIL
                    || template.getSendChannel() == NotifySendChannelEnum.MULTI) {
                mailService.sendMail(email, subject, content);
            }

            notify.setSendStatus(NotifySendStatusEnum.SUCCESS);
            notify.setSendTime(LocalDateTime.now());
            userNotifyMapper.updateById( notify);

        } catch (Exception e) {
            notify.setSendStatus(NotifySendStatusEnum.FAILED);
            notify.setSendTime(LocalDateTime.now());
            userNotifyMapper.updateById(notify);
            throw new BusinessException("发送通知失败: " + e.getMessage());
        }
    }
}