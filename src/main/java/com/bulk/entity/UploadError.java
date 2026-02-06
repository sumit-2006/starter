package com.bulk.entity;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Table(name = "upload_errors")
@NoArgsConstructor
@Entity
public class UploadError extends Model {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="file_upload_id",nullable = false)
  private Long fileUploadId;

  @Column(name="error_message",length = 500)
  private String errorMessage;

  @Column(name="row_num", nullable = false)
  private Integer  rowNumber;

  @Column(name="raw_data",columnDefinition = "TEXT")
  private String rawData;

  @WhenCreated
  @Column(name="created_at")
  private Instant createdAt;

  public UploadError(Long fileUploadId, String errorMessage, Integer rowNumber, String rawData) {
    this.fileUploadId = fileUploadId;
    this.errorMessage = errorMessage;
    this.rowNumber = rowNumber;
    this.rawData = rawData;
  }
}
