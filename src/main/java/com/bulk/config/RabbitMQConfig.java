package com.bulk.config;

import io.vertx.rabbitmq.RabbitMQOptions;

public class RabbitMQConfig {
  public static RabbitMQOptions getOptions() {
    String host = System.getenv("RABBITMQ_HOST");
    String uri=System.getenv("RABBITMQ_URI");
    if (host == null) host = "localhost"; //default hai
    return new RabbitMQOptions().setUri(uri)
      .setAutomaticRecoveryEnabled(true);
  }
}
