package com.bulk.starter;

import com.bulk.repository.CustomerRepository;
import com.bulk.repository.FileUploadRepository;
import com.bulk.service.CsvProcessorService;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;


import java.io.ByteArrayInputStream;

public class MainVerticle extends VerticleBase {

  @Override
  public Future<?> start() {
    FileUploadRepository fileUploadRepository = new FileUploadRepository();
    CustomerRepository customerRepository = new CustomerRepository();
    CsvProcessorService csvProcessorService = new CsvProcessorService(vertx, fileUploadRepository, customerRepository);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.post("/upload").handler(ctx -> {

      if (ctx.fileUploads().isEmpty()) {
        ctx.response().setStatusCode(400).end("No files uploaded");
        return;
      }

      for (io.vertx.ext.web.FileUpload f : ctx.fileUploads()) {
        try {
          Buffer fileBuffer = vertx.fileSystem().readFileBlocking(f.uploadedFileName());

          ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBuffer.getBytes());

          csvProcessorService.processFile(f.fileName(), inputStream);

        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("Failed to buffer file: " + f.fileName());
        }
      }

      ctx.response()
        .putHeader("content-type", "text/plain")
        .end("Upload started successfully.");
    });
    router.route("/*").handler(StaticHandler.create());

    return vertx.createHttpServer()
      .requestHandler(router) // Pass the router here!
      .listen(8080)           // Listen on port 8080
      .onSuccess(server -> System.out.println("Server started on port " + server.actualPort()))
      .onFailure(err -> System.err.println("Failed to start server: " + err.getMessage()));
  }
}
