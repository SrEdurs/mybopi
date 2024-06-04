package es.mybopi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.mybopi.model.EmailDTO;
import es.mybopi.service.EmailService;
import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/send-email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    private ResponseEntity<String> sendEmail(@RequestBody EmailDTO email) throws MessagingException {

        emailService.sendMail(email);
        return ResponseEntity.ok("Email enviado");
    }
}
