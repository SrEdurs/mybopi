package es.mybopi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.mybopi.service.PaymentService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        return ResponseEntity.ok(paymentService.getToken());
    }

}
