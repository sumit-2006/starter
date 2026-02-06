package com.bulk.starter;

import com.bulk.config.RabbitMQConfig;
import com.bulk.entity.FileUpload;
import com.bulk.repository.CustomerRepository;
import com.bulk.repository.FileUploadRepository;
import com.bulk.service.CsvProcessorService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.VerticleBase;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.rabbitmq.RabbitMQClient;


import java.io.ByteArrayInputStream;

public class MainVerticle extends VerticleBase {

        private RabbitMQClient rabbitClient;

  @Override
  public Future<?> start() {
    FileUploadRepository fileUploadRepository = new FileUploadRepository();
    CustomerRepository customerRepository = new CustomerRepository();
    CsvProcessorService csvProcessorService = new CsvProcessorService(vertx, fileUploadRepository, customerRepository);

    // 1. Start RabbitMQ Client for the Publisher
    rabbitClient = RabbitMQClient.create(vertx, RabbitMQConfig.getOptions());
    rabbitClient.start().onSuccess(v -> System.out.println("MainVerticle connected to RabbitMQ"));

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    DeploymentOptions workerOpts = new DeploymentOptions()
      .setThreadingModel(ThreadingModel.WORKER)
      .setInstances(4);

    vertx.deployVerticle(WorkerVerticle.class.getName(), workerOpts);

    /*router.post("/upload").handler(ctx -> {
      if (ctx.fileUploads().isEmpty()) {
        ctx.response().setStatusCode(400).end("No files uploaded");
        return;
      }

      JsonArray responseData = new JsonArray();

      for (io.vertx.ext.web.FileUpload f : ctx.fileUploads()) {
        try {
          Buffer fileBuffer = vertx.fileSystem().readFileBlocking(f.uploadedFileName());
          ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBuffer.getBytes());

          Long id = csvProcessorService.saveFile(f.fileName(), inputStream);

          if (id != -1L) {
            String message = String.valueOf(id);

            rabbitClient.basicPublish("", WorkerVerticle.QUEUE_NAME, io.vertx.core.buffer.Buffer.buffer(message))
              .onSuccess(pubResult -> System.out.println("Queued File ID: " + id))
              .onFailure(err -> System.err.println("Failed to queue: " + err.getMessage()));
          }

          if (id != null) {
            responseData.add(new JsonObject()
              .put("fileName", f.fileName())
              .put("id", id));
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      ctx.json(responseData);
    });*/

    router.post("/upload").handler(ctx -> {
      if (ctx.fileUploads().isEmpty()) {
        ctx.response().setStatusCode(400).end("No files uploaded");
        return;
      }

      JsonArray responseData = new JsonArray();

      // We use a List of Futures to track when ALL files are processed
      java.util.List<Future<Void>> futures = new java.util.ArrayList<>();

      for (io.vertx.ext.web.FileUpload f : ctx.fileUploads()) {

        // 1. Wrap Heavy Work in executeBlocking
        Future<Void> fileFuture = vertx.executeBlocking(() -> {
          try {
            // BLOCKING CODE GOES HERE (Safe now!)
            Buffer fileBuffer = vertx.fileSystem().readFileBlocking(f.uploadedFileName());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBuffer.getBytes());

            // Save to DB
            Long id = csvProcessorService.saveFile(f.fileName(), inputStream);

            // Pass ID to next step
            return id;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }).compose(id -> {
          // 2. BACK ON EVENT LOOP: Send to RabbitMQ
          if (id != -1L) {
            responseData.add(new JsonObject().put("fileName", f.fileName()).put("id", id));

            String message = String.valueOf(id);
            return rabbitClient.basicPublish("", WorkerVerticle.QUEUE_NAME, Buffer.buffer(message))
              .onSuccess(v -> System.out.println("Queued File ID: " + id));
          }
          return Future.succeededFuture();
        });

        futures.add(fileFuture);
      }

      // 3. When ALL files are queued, send response to Frontend
      Future.all(futures)
        .onSuccess(v -> ctx.json(responseData))
        .onFailure(err -> ctx.response().setStatusCode(500).end("Upload failed: " + err.getMessage()));
    });
    router.get("/status/:id").handler(ctx -> {
      Long id = Long.valueOf(ctx.pathParam("id"));
      FileUpload file = fileUploadRepository.findById(id);
      if (file != null) {
        ctx.json(JsonObject.mapFrom(file));
      } else {
        ctx.response().setStatusCode(404).end();
      }
    });
    router.route("/*").handler(StaticHandler.create());

    return rabbitClient.start()
      .compose(v -> {
        System.out.println("MainVerticle connected to RabbitMQ");
        return vertx.createHttpServer()
          .requestHandler(router)
          .listen(8080);
      })
      .onSuccess(server -> System.out.println("Server started on port " + server.actualPort()))
      .onFailure(err -> System.err.println("Startup failed: " + err.getMessage()));
  }
}
