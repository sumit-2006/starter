package com.bulk.config;

import io.vertx.rabbitmq.RabbitMQOptions;

public class RabbitMQConfig {
  public static RabbitMQOptions getOptions() {
    return new RabbitMQOptions()
      .setUri("amqp://guest:guest@localhost:5672")
      .setAutomaticRecoveryEnabled(true);
  }
}
