package com.portfolio.service;

import com.portfolio.dto.ContactDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.to}")
    private String toEmail;

    @Value("${app.mail.from:}")
    private String fromEmail;

    public void sendContactEmail(ContactDTO dto) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isEmpty()) {
            message.setFrom(fromEmail);
        }
        message.setTo(toEmail);
        message.setSubject("Nuevo mensaje de contacto");
        message.setText(buildBody(dto));
        mailSender.send(message);
    }

    private String buildBody(ContactDTO dto) {
        return "Nombre: " + dto.getName() + "\n" +
                "Email: " + dto.getEmail() + "\n\n" +
                (dto.getMessage() == null ? "" : dto.getMessage());
    }
}
