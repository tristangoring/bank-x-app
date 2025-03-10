package com.example.demo.controllers;

import com.example.demo.dtos.TransactionDTO;
import com.example.demo.entities.BankZTransaction;
import com.example.demo.entities.Transaction;
import com.example.demo.services.BankZReconciliationService;
import com.example.demo.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bankz/transactions")
public class BankZController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BankZReconciliationService bankZReconciliationService;

    @PostMapping("single")
    public Transaction makeSinglePayment(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.performOffUsPayment(transactionDTO);
    }

    @PostMapping("multiple")
    public List<Transaction> makeMultiplePayments(@RequestBody List<TransactionDTO> transactionDTOs) {
        return transactionService.performMultipleOffUsPayments(transactionDTOs);
    }

    @PostMapping("daily-report")
    public ResponseEntity<List<BankZTransaction>> makeReconciliationUpload(@RequestBody List<BankZTransaction> transactions) {
        List<BankZTransaction> batch = bankZReconciliationService.saveNewBatch(transactions);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(batch);
    }
}
