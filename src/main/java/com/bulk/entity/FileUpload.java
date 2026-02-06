package com.bulk.entity;

import com.bulk.enums.Status;
import io.ebean.Model;
import io.ebean.annotation.Length;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "file_uploads")
public class FileUpload extends Model {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "filename", length = 255)
  private String fileName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status",length = 20)
  private Status status=Status.PENDING;

  @Column(name = "total_records")
  private Integer totalRecords;

  @Column(name = "failure_count")
  private Integer failureRecords;

  @Column(name = "success_count")
  private Integer successRecords;

  @WhenCreated
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @WhenModified
  @Column(name = "updated_at")
  private Instant updatedAt;



}
