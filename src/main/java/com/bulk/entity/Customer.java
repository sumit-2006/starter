package com.bulk.entity;

import io.ebean.Model;
import io.ebean.annotation.NotNull;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "customers")
public class Customer extends Model{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name="file_upload_id")
  private Long fileUploadId;

  @Column(length=150)
  private String name;

  @Column(length=255)
  private String email;

  @Column(length=20)
  private String mobile;

  @Column(name="dob")
  private LocalDate dob;

  @WhenCreated
  @Column(name="created_at",updatable=false)
  private Instant created;

  public Customer(Long fileUploadId, String name, String email, String mobile,LocalDate dob) {
    this.fileUploadId = fileUploadId;
    this.name = name;
    this.email = email;
    this.mobile = mobile;
    this.dob = dob;
  }
}
