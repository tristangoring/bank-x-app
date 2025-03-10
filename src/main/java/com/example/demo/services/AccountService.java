package com.example.demo.services;

import com.example.demo.Repositories.AccountRepository;
import com.example.demo.entities.Account;
import com.example.demo.exceptions.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account saveAccount(final Account account) {
        return accountRepository.save(account);
    }

    public Account getAccount(final String id) {
        Optional<Account> account = accountRepository.findById(id);

        if (account.isPresent()) {
            return account.get();
        } else {
            throw new AccountNotFoundException("Account not found");
        }
    }

}
