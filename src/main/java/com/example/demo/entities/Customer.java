package com.example.demo.entities;

import com.example.demo.exceptions.AccountNotFoundException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="app_customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String email;
    private String cellphone;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Account> accounts;

    public Account getAccountById(String id) {
        Optional<Account> customerAccount = accounts.stream().filter(account -> account.getId().equals(id)).findFirst();
        if (customerAccount.isPresent()) {
            return customerAccount.get();
        } else {
            throw new AccountNotFoundException("Account not found");
        }
    }
}
