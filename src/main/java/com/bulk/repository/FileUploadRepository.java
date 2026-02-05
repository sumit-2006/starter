package com.bulk.repository;

import com.bulk.entity.FileUpload;
import com.bulk.enums.Status;
import io.ebean.DB;

public class FileUploadRepository {
  public FileUpload create(String fileName, byte[] fileContent) {
    FileUpload fileUpload=new FileUpload();
    fileUpload.setFileName(fileName);
    fileUpload.setContent(fileContent);
    fileUpload.save();
    return fileUpload;
  }

  public void updateStatus(Long id, Status status, int success, int failure) {
    FileUpload file= DB.find(FileUpload.class,id);
    if (file!=null) {
      file.setStatus(status);
      file.setSuccessRecords(success);
      file.setFailureRecords(failure);
      file.save();
    }
  }
}
