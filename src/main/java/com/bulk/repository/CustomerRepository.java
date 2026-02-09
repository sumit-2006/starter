package com.bulk.repository;

import com.bulk.entity.Customer;
import com.bulk.entity.UploadError;
import io.ebean.DB;
import io.ebean.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerRepository {
  public void saveBatch(List<Customer> customers){
    try(Transaction tx= DB.beginTransaction())
    {
      tx.setBatchMode(true);
      tx.setBatchSize(1000);
      tx.setGetGeneratedKeys(false);
      DB.saveAll(customers);

      tx.commit();
    }
  }

  public void saveErrors(List<UploadError> errors) {
    if(errors.isEmpty()) return;
    DB.saveAll(errors);
  }

  public Set<String> findExistingEmails(List<String> emails) {
    if (emails.isEmpty()) return new HashSet<>();

    return new HashSet<>(
      DB.find(Customer.class)
        .select("email")
        .where().in("email", emails)
        .findSingleAttributeList()
    );
  }
}
