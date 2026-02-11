package com.bulk.starter;

import com.bulk.config.RabbitMQConfig;
import com.bulk.service.EmailService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

public class EmailWorkerVerticle extends AbstractVerticle {
  public static final String EMAIL_QUEUE = "email_queue";
  private RabbitMQClient client;
  private EmailService emailService;

  @Override
  public void start(Promise<Void> startPromise) {
    emailService = new EmailService(vertx);
    client = RabbitMQClient.create(vertx, RabbitMQConfig.getOptions());

    client.start()
      .compose(v -> client.queueDeclare(EMAIL_QUEUE, true, false, false))
      .compose(declareOk -> client.basicConsumer(EMAIL_QUEUE))
      .onSuccess(consumer -> {
        System.out.println("Email Worker connected and listening to " + EMAIL_QUEUE);

        consumer.handler(message -> {
          try {
            JsonObject emailData = new JsonObject(message.body().toString());
            System.out.println("Picked up email task for File ID: " + emailData.getLong("fileId"));
            emailService.sendEmailFromJson(emailData);

            client.basicAck(message.envelope().getDeliveryTag(), false);
          } catch (Exception e) {
            System.err.println("Failed to process email message: " + e.getMessage());
            client.basicNack(message.envelope().getDeliveryTag(), false, false);
          }
        });
        startPromise.complete();
      })
      .onFailure(startPromise::fail);
  }
}
