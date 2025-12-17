package com.foodweb.foodapi.service;

import com.foodweb.foodapi.request.ContactRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendContactMessage(String toEmail, ContactRequest req) {

        String subject = "New Contact Form Message from " + req.getFirstName();

        String body = "Name: " + req.getFirstName() + " " + req.getLastName() +
                "\nEmail: " + req.getEmail() +
                "\n\nMessage:\n" + req.getMessage();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
