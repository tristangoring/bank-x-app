package com.example.demo.entities;

import com.example.demo.Enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="app_account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private AccountType type;
    private String customerId;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Transaction> transactions;
}
