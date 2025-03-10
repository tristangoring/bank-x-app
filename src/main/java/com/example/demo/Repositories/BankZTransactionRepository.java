package com.example.demo.Repositories;

import com.example.demo.entities.BankZTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankZTransactionRepository extends JpaRepository<BankZTransaction, String> {
    List<BankZTransaction> findAllByProcessed(boolean processed);
}
