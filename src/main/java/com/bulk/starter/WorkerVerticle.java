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
    client = RabbitMQClient.create(vertx, RabbitMQConfig.getOptions());
    csvProcessorService = new CsvProcessorService(vertx, fileRepo, customerRepo,client);



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


                long deliveryTag = message.envelope().getDeliveryTag();

                vertx.executeBlocking(() -> {
                  csvProcessorService.processFileById(fileId);
                  return null;
                }).onComplete(ar -> {
                  if (ar.succeeded()) {
                    client.basicAck(deliveryTag, false);
                  } else {
                    client.basicNack(deliveryTag, false, false);
                  }
                });







          } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
          }
        });

        startPromise.complete();
      })
      .onFailure(startPromise::fail);
  }
}
