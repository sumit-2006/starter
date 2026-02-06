package com.bulk.entity;

import io.ebean.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_content")
public class FileContent extends Model {

  @Id
  @Column(name="file_upload_id")
  public Long fileUploadId;

  @Lob
  @Column(name="raw_csv_content",columnDefinition = "LONGTEXT")
  public String rawCsvContent;
}
