package com.bulk.repository;

import com.bulk.entity.Customer;
import io.ebean.DB;
import io.ebean.Transaction;

import java.util.List;

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

  public void saveErrors(List<Customer> errors) {
    if(errors.isEmpty()) return;
    DB.saveAll(errors);
  }
}
