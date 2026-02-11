package com.bulk.service;

import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.core.json.JsonObject;

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

  public void sendEmailFromJson(JsonObject emailData) {
    String recipient = emailData.getString("recipient");
    if (recipient == null || recipient.isEmpty()) return;

    MailMessage message = new MailMessage();
    message.setFrom("no-reply@bulkprocessor.com");
    message.setTo(recipient);
    message.setSubject("File Processing Complete: File #" + emailData.getLong("fileId"));

    String htmlBody = String.format(
      "<h3>Processing Complete</h3>" +
        "<p><b>Status:</b> %s</p>" +
        "<ul>" +
        "<li><b>Success Records:</b> %d</li>" +
        "<li><b>Failed Records:</b> %d</li>" +
        "</ul>",
      emailData.getString("status"),
      emailData.getInteger("success"),
      emailData.getInteger("failure")
    );

    message.setHtml(htmlBody);

    mailClient.sendMail(message)
      .onSuccess(result -> {
        System.out.println("Email sent successfully to " + recipient);
      })
      .onFailure(err -> {
        System.err.println("Failed to send email: " + err.getMessage());
      });
  }
}
