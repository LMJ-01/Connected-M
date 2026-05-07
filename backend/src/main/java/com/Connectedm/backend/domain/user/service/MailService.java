package com.Connectedm.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    // ✨ UserService에서 호출하는 바로 그 메서드입니다!
    // 파라미터로 이메일(String)과 토큰(String) 두 개를 받습니다.
    public void sendResetLink(String toEmail, String token) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom("Connected M <dlaudwns0903@gmail.com>");
            helper.setSubject("[Connected M] 비밀번호 재설정 안내");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String html = "<html><body>" +
                    "<p>안녕하세요. Connected M입니다.</p>" +
                    "<p>비밀번호 재설정을 위해 아래 링크를 클릭해 주세요.</p>" +
                    "<p><a href=\"" + resetUrl + "\" target=\"_blank\">비밀번호 재설정 바로가기</a></p>" +
                    "<p>또는 아래 URL을 복사하여 브라우저에 붙여넣어 주세요:</p>" +
                    "<p><span style=\"word-break: break-all;\">" + resetUrl + "</span></p>" +
                    "<p>이 링크는 <strong>30분 동안만 유효</strong>합니다.</p>" +
                    "</body></html>";

            helper.setText(html, true);

            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}