package com.example.demo.controllers;

import com.example.demo.dtos.TransactionDTO;
import com.example.demo.entities.Account;
import com.example.demo.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("{customerId}/transfer")
    public List<Account> interAccountTransfer(@PathVariable String customerId, @RequestBody TransactionDTO transactionDTO) {
        return transactionService.performInterAccountTransfer(customerId, transactionDTO);
    }

    @PostMapping("{customerId}/payment")
    public List<Account> makePayment(@PathVariable String customerId, @RequestBody TransactionDTO transactionDTO) {
        return transactionService.performOnUsPayment(customerId, transactionDTO);
    }
}
