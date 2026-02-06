package com.bulk.starter;

import com.bulk.config.RabbitMQConfig;
import com.bulk.repository.CustomerRepository;
import com.bulk.repository.FileUploadRepository;
import com.bulk.service.CsvProcessorService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.rabbitmq.RabbitMQClient;

public class WorkerVerticle extends AbstractVerticle {
  public static final String QUEUE_NAME = "csv_upload_queue";
  private RabbitMQClient client;
  private CsvProcessorService csvProcessorService;

  @Override
  public void start(Promise<Void> startPromise) {
    FileUploadRepository fileRepo = new FileUploadRepository();
    CustomerRepository customerRepo = new CustomerRepository();
    csvProcessorService = new CsvProcessorService(vertx, fileRepo, customerRepo);

    client = RabbitMQClient.create(vertx, RabbitMQConfig.getOptions());

    client.start()
      .compose(v -> {
        System.out.println("Worker Connected to RabbitMQ");
        return client.queueDeclare(QUEUE_NAME, true, false, false);
      })
      .compose(declareOk -> {
        return client.basicConsumer(QUEUE_NAME);
      })
      .onSuccess(consumer -> {
        consumer.handler(message -> {
          try {
            Long fileId = Long.valueOf(message.body().toString());
            System.out.println("Worker received File ID: " + fileId);

            csvProcessorService.processFileById(fileId);
            long deliveryTag = message.envelope().getDeliveryTag();

            client.basicAck(deliveryTag , false)
              .onFailure(err -> System.err.println("Ack failed: " + err.getMessage()));

          } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
          }
        });

        startPromise.complete();
      })
      .onFailure(startPromise::fail);
  }
}
