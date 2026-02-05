package com.bulk.service;

import com.bulk.entity.FileUpload;
import com.bulk.repository.CustomerRepository;
import com.bulk.repository.FileUploadRepository;
import io.vertx.core.Vertx;

import java.io.InputStream;

public class CsvProcessorService {
  private final Vertx vertx;
  private final CustomerRepository customerRepository;
  private final FileUploadRepository fileUploadRepository;

  public CsvProcessorService(Vertx vertx,FileUploadRepository fileUploadRepository,CustomerRepository customerRepository) {
    this.vertx = vertx;
    this.customerRepository = customerRepository;
    this.fileUploadRepository = fileUploadRepository;
  }

  public void processFile(String filename, InputStream inputStream)
  {
    FileUpload fileUpload=fileUploadRepository.create(filename);

  }
}
