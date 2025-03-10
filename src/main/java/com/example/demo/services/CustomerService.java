package com.example.demo.services;

import com.example.demo.Enums.AccountType;
import com.example.demo.Enums.Bank;
import com.example.demo.Enums.TransactionType;
import com.example.demo.Repositories.CustomerRepository;
import com.example.demo.entities.Account;
import com.example.demo.entities.Customer;
import com.example.demo.entities.Transaction;
import com.example.demo.exceptions.CustomerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer createCustomer(final Customer customer) {
        this.createCurrentAccount(customer);
        this.createSavingsAccount(customer);

        return saveCustomer(customer);
    }

    public Customer saveCustomer(final Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer getCustomer(final String id) {
        Optional<Customer> customer = customerRepository.findById(id);

        if (customer.isPresent()) {
            return customer.get();
        } else {
            throw new CustomerNotFoundException("Customer not found");
        }
    }

    private void createCurrentAccount(final Customer customer) {
        Account currentAccount = new Account();
        currentAccount.setType(AccountType.CURRENT);
        currentAccount.setBalance(BigDecimal.ZERO);

        customer.setAccounts(new ArrayList<>());
        customer.getAccounts().add(currentAccount);
    }

    private void createSavingsAccount(final Customer customer) {
        Account savingsAccount = new Account();
        savingsAccount.setType(AccountType.SAVINGS);
        savingsAccount.setBalance(BigDecimal.valueOf(500));

        this.depositJoiningBonus(savingsAccount);
        customer.getAccounts().add(savingsAccount);
    }

    private void depositJoiningBonus(final Account account) {
        Transaction initialDeposit = new Transaction();
        initialDeposit.setAmount(BigDecimal.valueOf(500));
        initialDeposit.setReference("Joining Bonus");
        initialDeposit.setFromBank(Bank.BANK_X);
        initialDeposit.setType(TransactionType.JOINING_BONUS);
        initialDeposit.setDate(LocalDateTime.now(Clock.systemUTC()));

        account.setTransactions(new ArrayList<>());
        account.getTransactions().add(initialDeposit);
    }

}
