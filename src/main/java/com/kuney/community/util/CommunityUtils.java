package com.kuney.community.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.kuney.community.util.Constants.Location;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author kuneychen
 * @since 2022/6/11 22:29
 */
@Component
@Slf4j
public class CommunityUtils {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${user.image.path}")
    private String path;
    @Value("${user.image.upload-location}")
    private String location;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.domain}")
    private String domain;

    /**
     * 发送简单邮件
     * @param to 接收方邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("发送邮件失败：{}", e.getMessage());
        }
    }

    /**
     * 上传图片
     * @param file 图片
     * @param suffix 文件后缀
     * @return
     */
    public String uploadFile(MultipartFile file, String suffix) {
        String url = null;
        switch (location) {
            case Location.LOCAL:
                url = uploadToLocal(file, suffix);
                break;
            case Location.REMOTE:
                // TODO 上传到云服务
                break;
        }
        return url;
    }

    private String uploadToLocal(MultipartFile file, String suffix) {
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
        String dest = path + fileName;
        try {
            file.transferTo(new File(dest));
        } catch (IOException e) {
            log.error("上传文件失败：{}", e.getMessage());
        }
        String url = domain + "/" + contextPath + "/user/header/" + fileName;
        return url;
    }
}
