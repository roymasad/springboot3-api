package com.roytemplates.springboot3_api.service;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/// Service class for sending emails using SendGrid.
/// This service provides functionality for sending emails to recipients using the SendGrid API.
@Service
public class EmailService {

    @Value("${sendgrid.key}")
    private String sendgridKey;

    @Value("${email.from}")
    private String emailFrom;

    // Service for sending emails
    public void sendEmail(String recipient, String subject, String body) throws IOException {
        // Send email implementation

        Email from = new Email(emailFrom);
        Email to = new Email(recipient);
        Content content = new Content("text/plain", body);
        
        Mail mail = new Mail(from, subject, to, content);

        //System.getenv("SENDGRID_API_KEY");
        SendGrid sg = new SendGrid(sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }

    }
    
}
