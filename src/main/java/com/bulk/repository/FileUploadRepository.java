package com.bulk.repository;

import com.bulk.entity.FileContent;
import com.bulk.entity.FileUpload;
import com.bulk.enums.Status;
import io.ebean.DB;
import io.ebean.Transaction;

import java.nio.charset.StandardCharsets;

public class FileUploadRepository {
  public FileUpload create(String fileName, byte[] fileContent) {
    String contentString =new String(fileContent, StandardCharsets.UTF_8);

    try(Transaction transaction=DB.beginTransaction()) {
      try {

        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName(fileName);
        fileUpload.setStatus(Status.PENDING);
        fileUpload.setSuccessRecords(0);
        fileUpload.setFailureRecords(0);
        fileUpload.save();

        FileContent content = new FileContent(fileUpload.getId(), contentString);
        content.save();

        transaction.commit();
        return fileUpload;
      } catch (Exception e) {
        transaction.rollback();
        throw e;
      }
    }
  }

  public void updateStatus(Long id, Status status, int success, int failure) {
    DB.update(FileUpload.class)
      .set("status", status)
      .set("successRecords", success)
      .set("failureRecords", failure)
      .where().idEq(id)
      .update();
  }
  public String getContent(Long fileUploadId) {
    FileContent fc = DB.find(FileContent.class, fileUploadId);
    return (fc != null) ? fc.rawCsvContent : null;
  }
  public FileUpload findById(Long id)
  {
    return DB.find(FileUpload.class, id);
  }

}
