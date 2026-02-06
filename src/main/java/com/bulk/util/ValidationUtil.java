package com.bulk.util;

import com.bulk.entity.Customer;
import com.bulk.entity.UploadError;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtil {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
  private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{10}$");

  public static RowResult validateAndMap(Long fileId, int rowNum, String line) {
    if (line == null || line.trim().isEmpty()) {
      return null;
    }

    String[] data = line.split(",");

    if (data.length < 3) {
      return new RowResult(new UploadError(fileId,"Incomplete Data: Missing columns", rowNum,  line));
    }

    String name = data[0].trim();
    String email = data[1].trim();
    String mobile = data[2].trim();
    LocalDate dob = null;
    if (data.length > 3 && !data[3].trim().isEmpty()) {
      try {
        // Default format expected: YYYY-MM-DD
        dob = LocalDate.parse(data[3].trim());
      } catch (DateTimeParseException e) {
        return new RowResult(new UploadError(fileId, "Invalid Date Format (Expected YYYY-MM-DD)", rowNum, line));
      }
    }

    if (name.isEmpty()) {
      return new RowResult(new UploadError(fileId, "Name is required",rowNum,  line));
    }

    if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
      return new RowResult(new UploadError(fileId,"Invalid Email Format", rowNum,  line));
    }

    if (mobile.isEmpty() || !MOBILE_PATTERN.matcher(mobile).matches()) {
      return new RowResult(new UploadError(fileId,"Invalid Mobile (Must be 10 digits)", rowNum,  line));
    }

    Customer customer = new Customer(fileId, name, email, mobile,dob);
    customer.setCreated(new java.sql.Timestamp(System.currentTimeMillis()));
    return new RowResult(customer);
  }
}
