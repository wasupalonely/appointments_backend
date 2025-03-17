package com.juandmv.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    /**
     * Sends an email with well-formatted HTML content
     *
     * @param recipient  Email address of the recipient
     * @param subject    Email subject
     * @param name       Recipient's name
     * @param message    Main message content
     * @throws MessagingException if there are problems sending the email
     */
    public void sendEmail(String recipient, String subject, String name, String message) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(sender);
        helper.setTo(recipient);
        helper.setSubject(subject);

        String htmlContent = generateHtmlTemplate(name, message);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    /**
     * Generates an attractive HTML template for the email
     */
    private String generateHtmlTemplate(String name, String message) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 0; }\n" +
                "        .container { border: 1px solid #ddd; border-radius: 5px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n" +
                "        .header { background-color: #4a86e8; color: white; padding: 20px; text-align: center; }\n" +
                "        .content { background-color: #ffffff; padding: 20px 30px; }\n" +
                "        .message { background-color: #f9f9f9; padding: 15px; border-left: 4px solid #4a86e8; margin: 20px 0; }\n" +
                "        .footer { background-color: #f1f1f1; font-size: 12px; text-align: center; padding: 15px; color: #666; }\n" +
                "        h1 { margin: 0; font-size: 24px; }\n" +
                "        p { margin-bottom: 15px; }\n" +
                "        .button { display: inline-block; background-color: #4a86e8; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Notification</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hola, <strong>" + name + "</strong>,</p>\n" +
                "            <div class=\"message\">\n" +
                "                <p>" + message + "</p>\n" +
                "            </div>\n" +
                "            <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>\n" +
                "            <p>Best regards,<br>The " + getCompanyName() + " Team</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>This is an automated message. Please do not reply to this email.</p>\n" +
                "            <p>&copy; " + java.time.Year.now().getValue() + " " + getCompanyName() + ". All rights reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Returns the company name to use in the template
     */
    private String getCompanyName() {
        return "MediTec";
    }
}