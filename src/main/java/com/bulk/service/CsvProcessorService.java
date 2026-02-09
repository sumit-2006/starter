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
import java.nio.charset.StandardCharsets;
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

  /*public Long processFile(String filename, InputStream inputStream) {
    try {
      byte[] fileBytes = inputStream.readAllBytes();
      if (fileBytes.length == 0) throw new RuntimeException("Empty File");
      FileUpload fileUpload = fileUploadRepository.create(filename, fileBytes);
      Long fileUploadId = fileUpload.getId();
      String csvContent = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
      long totalLines = 0;
      try (BufferedReader counter = new BufferedReader(new StringReader(csvContent))) {
        totalLines = counter.lines().count();
      }
      int totalRecords = (int) Math.max(0, totalLines - 1);

      fileUpload.setTotalRecords(totalRecords);
      io.ebean.DB.save(fileUpload);
      vertx.executeBlocking(() -> {
          fileUploadRepository.updateStatus(fileUploadId, Status.PROCESSING, 0, 0);

          List<Customer> validCustomers = new ArrayList<>();
          List<UploadError> errorRows = new ArrayList<>();

          try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            int rowNum = 0;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
              try { Thread.sleep(50); } catch (InterruptedException e) {}

              rowNum++;
              RowResult result = ValidationUtil.validateAndMap(fileUploadId, rowNum, line);
              if (result == null) continue;

              if (result.isValid()) {
                validCustomers.add(result.getCustomer());
              } else {
                errorRows.add(result.getError());
              }

              if (rowNum % 50 == 0) {
                fileUploadRepository.updateStatus(fileUploadId, Status.PROCESSING, validCustomers.size(), errorRows.size());
              }
            }
          }

          if (!validCustomers.isEmpty()) {
            customerRepository.saveBatch(validCustomers);
          }
          if (!errorRows.isEmpty()) {
            customerRepository.saveErrors(errorRows);
          }

          Status finalStatus = errorRows.isEmpty() ? Status.COMPLETED : validCustomers.isEmpty() ? Status.FAILED : Status.PARTIAL_SUCCESS;

          fileUploadRepository.updateStatus(fileUploadId, finalStatus, validCustomers.size(), errorRows.size());

          return null;
        })
        .onSuccess(v -> {
          System.out.println("Processing complete: " + filename);
        })
        .onFailure(err -> {
          err.printStackTrace();
        });

return fileUploadId;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }*/

  public Long saveFile(String filename, InputStream inputStream) {
    try {
      byte[] fileBytes = inputStream.readAllBytes();
      if (fileBytes.length == 0) throw new RuntimeException("Empty File");

      FileUpload fileUpload = fileUploadRepository.create(filename, fileBytes);

      String csvContent = new String(fileBytes, StandardCharsets.UTF_8);
      long totalLines = 0;
      try (BufferedReader counter = new BufferedReader(new StringReader(csvContent))) {
        totalLines = counter.lines().count();
      }
      int totalRecords = (int) Math.max(0, totalLines - 1); // Subtract Header

      fileUpload.setTotalRecords(totalRecords);
      io.ebean.DB.save(fileUpload);

      return fileUpload.getId();

    } catch (Exception e) {
      e.printStackTrace();
      return -1L;
    }
  }

  public void processFileById(Long fileId) {
    try {
      String csvContent = null;
      int attempts = 0;

      while (csvContent == null && attempts < 5) {
        csvContent = fileUploadRepository.getContent(fileId);
        if (csvContent == null) {
          System.out.println("Attempt " + (attempts+1) + ": Content not found yet for ID " + fileId + ". Retrying...");
          attempts++;
          try { Thread.sleep(5000); } catch (InterruptedException e) {}
        }
      }


      if (csvContent == null || csvContent.isEmpty()) {
        System.err.println("FATAL: File content still NOT FOUND for ID: " + fileId + " after retries.");
        fileUploadRepository.updateStatus(fileId, Status.FAILED, 0, 0);
        return;
      }
      fileUploadRepository.updateStatus(fileId, Status.PROCESSING, 0, 0);

      List<Customer> validCustomers = new ArrayList<>();
      List<UploadError> errorRows = new ArrayList<>();

      try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
        String line;
        int rowNum = 0;
        reader.readLine();

        while ((line = reader.readLine()) != null) {
          rowNum++;
          // try { Thread.sleep(10); } catch (Exception e) {}

          RowResult result = ValidationUtil.validateAndMap(fileId, rowNum, line);
          if (result == null) continue;

          if (result.isValid()) {
            validCustomers.add(result.getCustomer());
          } else {
            errorRows.add(result.getError());
          }

          if (rowNum % 50 == 0) {
            fileUploadRepository.updateStatus(fileId, Status.PROCESSING, validCustomers.size(), errorRows.size());
          }
        }
      }

      if (!validCustomers.isEmpty()) customerRepository.saveBatch(validCustomers);
      if (!errorRows.isEmpty()) customerRepository.saveErrors(errorRows);

      Status finalStatus = errorRows.isEmpty() ? Status.COMPLETED :
        (validCustomers.isEmpty() ? Status.FAILED : Status.PARTIAL_SUCCESS);

      fileUploadRepository.updateStatus(fileId, finalStatus, validCustomers.size(), errorRows.size());

      System.out.println("Finished processing File ID: " + fileId);

    } catch (Exception e) {
      e.printStackTrace();
      fileUploadRepository.updateStatus(fileId, Status.FAILED, 0, 0);
    }
  }
}

