package com.example.demo.controllers;

import com.example.demo.entities.Account;
import com.example.demo.entities.Customer;
import com.example.demo.entities.Transaction;
import com.example.demo.exceptions.CustomerNotFoundException;
import com.example.demo.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    @GetMapping("{id}/accounts")
    public List<Account> getCustomerAccounts(@PathVariable String id) {
        return customerService.getCustomer(id).getAccounts();
    }

    @GetMapping("{id}/accounts/{accountId}/transactions")
    public List<Transaction> getCustomerAccountTransactions(@PathVariable String id, @PathVariable String accountId) {
        return customerService.getCustomer(id).getAccountById(accountId).getTransactions();
    }
}
