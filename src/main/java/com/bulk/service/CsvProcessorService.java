package com.bulk.service;

import com.bulk.entity.Customer;
import com.bulk.entity.FileUpload;
import com.bulk.entity.UploadError;
import com.bulk.enums.Status;
import com.bulk.repository.CustomerRepository;
import com.bulk.repository.FileUploadRepository;
import com.bulk.util.RowResult;
import com.bulk.util.ValidationUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.Promise;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class CsvProcessorService {
  private final Vertx vertx;
  private final CustomerRepository customerRepository;
  private final FileUploadRepository fileUploadRepository;

  public CsvProcessorService(Vertx vertx, FileUploadRepository fileUploadRepository, CustomerRepository customerRepository) {
    this.vertx = vertx;
    this.customerRepository = customerRepository;
    this.fileUploadRepository = fileUploadRepository;
  }

  public void processFile(String filename, InputStream inputStream) {
    try {
      byte[] fileBytes = inputStream.readAllBytes();
      if (fileBytes.length == 0) throw new RuntimeException("Empty File");
      FileUpload fileUpload = fileUploadRepository.create(filename, fileBytes);
      Long fileUploadId = fileUpload.getId();
      String csvContent = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
      vertx.executeBlocking(() -> {
          fileUploadRepository.updateStatus(fileUploadId, Status.PROCESSING, 0, 0);

          List<Customer> validCustomers = new ArrayList<>();
          List<UploadError> errorRows = new ArrayList<>();

          try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            int rowNum = 0;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
              rowNum++;
              RowResult result = ValidationUtil.validateAndMap(fileUploadId, rowNum, line);
              if (result == null) continue;

              if (result.isValid()) {
                validCustomers.add(result.getCustomer());
              } else {
                errorRows.add(result.getError());
              }
            }
          }

          if (!validCustomers.isEmpty()) {
            customerRepository.saveBatch(validCustomers);
          }
          if (!errorRows.isEmpty()) {
            customerRepository.saveErrors(errorRows); // still must match repo type
          }

          Status finalStatus = errorRows.isEmpty()
            ? Status.COMPLETED
            : validCustomers.isEmpty()
            ? Status.FAILED
            : Status.PARTIAL_SUCCESS;

          fileUploadRepository.updateStatus(
            fileUploadId,
            finalStatus,
            validCustomers.size(),
            errorRows.size()
          );

          return null; // REQUIRED
        })
        .onSuccess(v -> {
          System.out.println("Processing complete: " + filename);
        })
        .onFailure(err -> {
          err.printStackTrace();
        });



    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
