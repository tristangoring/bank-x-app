package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String fromAccountId;
    private String fromAccountReference;
    private String toAccountId;
    private String toAccountReference;
    private BigDecimal amount;
}
