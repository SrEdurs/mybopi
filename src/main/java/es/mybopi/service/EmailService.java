package es.mybopi.service;

import org.springframework.stereotype.Service;
import es.mybopi.model.EmailDTO;
import jakarta.mail.MessagingException;

@Service
public interface EmailService {
    void sendMail(EmailDTO email) throws MessagingException;
}
