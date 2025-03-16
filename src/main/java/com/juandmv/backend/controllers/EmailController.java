package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.EmailRequest;
import com.juandmv.backend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarEmail(@RequestBody EmailRequest request) {
        try {
            emailService.sendEmail(
                    request.getDestinatario(),
                    request.getAsunto(),
                    request.getNombre(),
                    request.getMensaje()
            );
            return ResponseEntity.ok("Correo enviado correctamente a " + request.getDestinatario());
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo: " + e.getMessage());
        }
    }
}
