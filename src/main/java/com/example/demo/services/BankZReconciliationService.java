package com.example.demo.services;

import com.example.demo.Repositories.BankZTransactionRepository;
import com.example.demo.entities.BankZTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankZReconciliationService {

    @Autowired
    private BankZTransactionRepository bankZTransactionRepository;

    public List<BankZTransaction> getAllUnprocessedTransactions() {
        return bankZTransactionRepository.findAllByProcessed(false);
    }

    public List<BankZTransaction> saveNewBatch(List<BankZTransaction> bankZTransactions) {
        for (BankZTransaction bankZTransaction : bankZTransactions) {
            bankZTransaction.setProcessed(false);
        }
        return bankZTransactionRepository.saveAll(bankZTransactions);
    }

    public BankZTransaction processTransaction(BankZTransaction transaction) {
        // TODO: Actually check Transaction table for reflecting record
        transaction.setProcessed(true);
        return bankZTransactionRepository.save(transaction);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void processBankZBatch() {
        List<BankZTransaction> batch = this.getAllUnprocessedTransactions();
        for (BankZTransaction bankZTransaction : batch) {
            this.processTransaction(bankZTransaction);
        }
    }
}
