package com.bulk.entity;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_content")
public class FileContent extends Model {

  @Id
  public Long fileUploadId;

  @Lob
  public String rawCsvContent;
}
