package com.bulk.config;

import io.vertx.rabbitmq.RabbitMQOptions;

public class RabbitMQConfig {
  public static RabbitMQOptions getOptions() {
    String host = System.getenv("RABBITMQ_HOST");
    if (host == null) host = "localhost"; //default hai
    return new RabbitMQOptions().setUri("amqp://guest:guest@" + host + ":5672")
      .setAutomaticRecoveryEnabled(true);
  }
}
