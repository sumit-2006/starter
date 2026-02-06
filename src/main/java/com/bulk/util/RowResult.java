package com.bulk.util;

import com.bulk.entity.Customer;
import com.bulk.entity.UploadError;
import lombok.Getter;

@Getter
public class RowResult {
  private final Customer customer;
  private final UploadError error;
  private final boolean valid;

  public RowResult(Customer customer) {
    this.customer = customer;
    this.error = null;
    this.valid = true;
  }

  public RowResult(UploadError error) {
    this.customer = null;
    this.error = error;
    this.valid = false;
  }
}
