package com.example.demo.entities;

import com.example.demo.Enums.Bank;
import com.example.demo.Enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="app_transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private BigDecimal amount;
    private String reference;
    private String accountReference;
    @Enumerated(EnumType.STRING)
    private Bank fromBank;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private LocalDateTime date;
    private String accountId;
}
