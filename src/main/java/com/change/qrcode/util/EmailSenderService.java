package com.change.qrcode.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
public class EmailSenderService {

    @Autowired
    private  JavaMailSender mailSender;

    public void sendEmail(String toEmail,String subject,String body){

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("adm.change.qr@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
    public void sendMimeEmail(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("contact@shopme.com", "Change QR Admin");
        helper.setTo(recipientEmail);

        String subject = "Sifrenizi yenileme linkiniz hazir";

        String content = "<p>Merhabalar,</p>"
                + "<p>Sifrenizi yenilemek istediniz.</p>"
                + "<p>Sifrenizi yenilemek icin linke tiklayiniz</p>"
                + "<p><a href=\"" + link + "\">Sifremi Degistir</a></p>"
                + "<br>"
                + "<p>Bu talebi siz olusturmadiysaniz bu maili silebilirsiniz.</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }
}
