package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.EmailRequest;
import com.juandmv.backend.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/email")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        logger.info("Attempting to send email to: {}", request.getReceiver());

        try {
            // Validate input
            if (request.getReceiver() == null || request.getReceiver().isEmpty()) {
                return ResponseEntity.badRequest().body("Receiver email is required");
            }

            emailService.sendEmail(
                    request.getReceiver(),
                    request.getSubject(),
                    request.getName(),
                    request.getMessage()
            );

            logger.info("Email successfully sent to: {}", request.getReceiver());
            return ResponseEntity.ok("Email successfully sent to " + request.getReceiver());

        } catch (MessagingException e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending email: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error when sending email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}