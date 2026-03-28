package com.yuntan.infra.mail.service;

public interface MailService {

    /**
     * 发送邮件
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendMail(String to, String subject, String content);

    /**
     * 异步发送邮件
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendMailAsync(String to, String subject, String content);

}
