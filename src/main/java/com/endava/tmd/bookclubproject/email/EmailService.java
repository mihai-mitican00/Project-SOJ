package com.endava.tmd.bookclubproject.email;

import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService implements EmailSender{

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private  JavaMailSender javaMailSender;

    @Async
    @Override
    public void sendEmail(String to, String email) {
        try{
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject("Confirm your account!");
            javaMailSender.send(mimeMessage);
        }catch (MessagingException ex){
            LOGGER.error("Email could not be sent", ex);
            throw new ApiBadRequestException("Email could not be sent");
        }
    }

}
