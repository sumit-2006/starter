package com.bulk.service;

import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;

public class EmailService {

  private final MailClient mailClient;

  public EmailService(Vertx vertx) {
    String host = System.getenv().getOrDefault("MAIL_HOST", "smtp.gmail.com");
    int port = Integer.parseInt(System.getenv().getOrDefault("MAIL_PORT", "587"));
    String username = System.getenv("MAIL_USER");
    String password = System.getenv("MAIL_PASS");

    MailConfig config = new MailConfig()
      .setHostname(host)
      .setPort(port)
      .setStarttls(StartTLSOptions.REQUIRED)
      .setUsername(username)
      .setPassword(password)
      .setTrustAll(true);

    this.mailClient = MailClient.createShared(vertx, config);
  }

  public void sendCompletionEmail(String recipient, String fileName, String status, int success, int failure) {
    if (recipient == null || recipient.isEmpty()) {
      System.out.println("No recipient email provided, skipping notification.");
      return;
    }

    MailMessage message = new MailMessage();
    message.setFrom("no-reply@bulkprocessor.com");
    message.setTo(recipient);
    message.setSubject("File Processing Complete: " + fileName);

    String htmlBody = String.format(
      "<h3>Processing Complete</h3>" +
        "<p><b>File:</b> %s</p>" +
        "<p><b>Status:</b> %s</p>" +
        "<ul>" +
        "<li><b>Success Records:</b> %d</li>" +
        "<li><b>Failed Records:</b> %d</li>" +
        "</ul>",
      fileName, status, success, failure
    );

    message.setHtml(htmlBody);

    mailClient.sendMail(message, result -> {
      if (result.succeeded()) {
        System.out.println("Email sent successfully to " + recipient);
      } else {
        System.err.println("Failed to send email: " + result.cause().getMessage());
        result.cause().printStackTrace();
      }
    });
  }
}
