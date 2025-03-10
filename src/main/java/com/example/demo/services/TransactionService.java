package com.example.demo.services;

import com.example.demo.Enums.AccountType;
import com.example.demo.Enums.Bank;
import com.example.demo.Enums.TransactionType;
import com.example.demo.Repositories.TransactionRepository;
import com.example.demo.dtos.TransactionDTO;
import com.example.demo.entities.Account;
import com.example.demo.entities.Customer;
import com.example.demo.entities.Transaction;
import com.example.demo.exceptions.AccountNotFoundException;
import com.example.demo.exceptions.IncorrectAccountTypeException;
import com.example.demo.exceptions.InsufficientFundsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final BigDecimal TRANSACTION_FEES_PERCENT = BigDecimal.valueOf(0.05 / 100);
    private static final BigDecimal INTEREST_ACCRUAL_PERCENT = BigDecimal.valueOf(0.5 / 100);

    public List<Account> performInterAccountTransfer(String customerId, TransactionDTO transactionDTO) {
        Customer customer = customerService.getCustomer(customerId);

        Account fromAccount = customer.getAccountById(transactionDTO.getFromAccountId());
        Account toAccount = customer.getAccountById(transactionDTO.getToAccountId());
        this.processTransfer(fromAccount, toAccount, transactionDTO);

        return customerService.saveCustomer(customer).getAccounts();
    }

    public List<Account> performOnUsPayment(String customerId, TransactionDTO transactionDTO) {
        Customer customer = customerService.getCustomer(customerId);

        Account fromAccount = customer.getAccountById(transactionDTO.getFromAccountId());
        Account toAccount = accountService.getAccount(transactionDTO.getToAccountId());
        this.processOnUsPayment(fromAccount, toAccount, transactionDTO);

        return customerService.saveCustomer(customer).getAccounts();
    }

    public Transaction performOffUsPayment(TransactionDTO transactionDTO) {
        Account account = accountService.getAccount(transactionDTO.getToAccountId());
        Transaction offUsPayment = null;
        boolean isDebit = transactionDTO.getAmount().compareTo(BigDecimal.ZERO) < 0;
        boolean isCredit = transactionDTO.getAmount().compareTo(BigDecimal.ZERO) > 0;

        if (isCredit || (isDebit && this.hasSufficientFunds(account, transactionDTO.getAmount().abs()))) {
            offUsPayment = generateOffUsPayment(account, transactionDTO);
        }

        this.accountService.saveAccount(account);

        if (isCredit && this.isSavingsAccount(account)) {
            this.generateInterestTransaction(account);
        } else if (isDebit && this.isCurrentAccount(account)) {
            this.generatePaymentFeesTransaction(account, transactionDTO.getAmount().abs());
        }

        return offUsPayment;
    }

    public List<Transaction> performMultipleOffUsPayments(List<TransactionDTO> transactionDTOs) {
        List<Transaction> transactions = new ArrayList<>();

        for (TransactionDTO transactionDTO : transactionDTOs) {
            try {
                transactions.add(performOffUsPayment(transactionDTO));
            } catch (AccountNotFoundException | InsufficientFundsException | IncorrectAccountTypeException e) {
                System.out.println(e.getMessage());
            }
        }

        return transactions;
    }

    private boolean hasSufficientFunds(final Account account, final BigDecimal amount) {
        boolean result = account.getBalance().compareTo(amount) >= 0;

        if (this.isSavingsAccount(account)) {
            throw new IncorrectAccountTypeException("Payments can only be made from CURRENT Accounts");
        } else if (!result) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        return true;
    }

    private boolean isCurrentAccount(final Account account) {
        return account.getType() == AccountType.CURRENT;
    }

    private boolean isSavingsAccount(final Account account) {
        return account.getType() == AccountType.SAVINGS;
    }

    private void processTransfer(final Account fromAccount, final Account toAccount, final TransactionDTO transactionDTO) {
        if (this.hasSufficientFunds(fromAccount, transactionDTO.getAmount())) {
            this.performTransferFrom(fromAccount, transactionDTO);
            this.performTransferTo(toAccount, transactionDTO);
        }
    }

    private void performTransferFrom(final Account account, final TransactionDTO transactionDTO) {
        Transaction transfer = new Transaction();
        transfer.setAmount(transactionDTO.getAmount().negate());
        transfer.setFromBank(Bank.BANK_X);
        transfer.setReference("Inter Account Transfer");
        transfer.setAccountReference(transactionDTO.getToAccountId());
        transfer.setType(TransactionType.INTER_ACCOUNT_TRANSFER);
        transfer.setDate(LocalDateTime.now(Clock.systemUTC()));

        if (account.getTransactions() == null) {
            account.setTransactions(new ArrayList<>());
        }
        account.setBalance(account.getBalance().add(transfer.getAmount()));
        account.getTransactions().add(transfer);

        this.notificationService.sendFromNotification(account, transfer);
    }

    private void performTransferTo(final Account account, final TransactionDTO transactionDTO) {
        Transaction transfer = new Transaction();
        transfer.setAmount(transactionDTO.getAmount());
        transfer.setFromBank(Bank.BANK_X);
        transfer.setReference("Inter Account Transfer");
        transfer.setAccountReference(transactionDTO.getFromAccountId());
        transfer.setType(TransactionType.INTER_ACCOUNT_TRANSFER);
        transfer.setDate(LocalDateTime.now(Clock.systemUTC()));

        if (account.getTransactions() == null) {
            account.setTransactions(new ArrayList<>());
        }
        account.setBalance(account.getBalance().add(transfer.getAmount()));
        account.getTransactions().add(transfer);

        this.notificationService.sendToNotification(account, transfer);
    }

    private void processOnUsPayment(final Account fromAccount, final Account toAccount, final TransactionDTO transactionDTO) {
        if (this.hasSufficientFunds(fromAccount, transactionDTO.getAmount()) && this.isCurrentAccount(fromAccount)) {
            this.performOnUsPaymentFrom(fromAccount, transactionDTO);
            this.performOnUsPaymentTo(toAccount, transactionDTO);
        } else {
            throw new IncorrectAccountTypeException("Payments can only be made from CURRENT accounts");
        }
    }

    private void performOnUsPaymentFrom(final Account account, final TransactionDTO transactionDTO) {
        Transaction payment = new Transaction();
        payment.setAmount(transactionDTO.getAmount().negate());
        payment.setFromBank(Bank.BANK_X);
        payment.setReference(transactionDTO.getFromAccountReference());
        payment.setAccountReference(transactionDTO.getToAccountId());
        payment.setType(TransactionType.PAYMENT);
        payment.setDate(LocalDateTime.now(Clock.systemUTC()));

        if (account.getTransactions() == null) {
            account.setTransactions(new ArrayList<>());
        }

        Transaction fees = this.generatePaymentFeesTransaction(account, transactionDTO.getAmount());
        account.setBalance(account.getBalance().add(payment.getAmount()).add(fees.getAmount()));
        account.getTransactions().add(payment);
        account.getTransactions().add(fees);

        this.notificationService.sendFromNotification(account, payment);
    }

    private void performOnUsPaymentTo(final Account account, final TransactionDTO transactionDTO) {
        Transaction payment = new Transaction();
        payment.setAmount(transactionDTO.getAmount());
        payment.setFromBank(Bank.BANK_X);
        payment.setReference(transactionDTO.getToAccountReference());
        payment.setAccountReference(transactionDTO.getFromAccountId());
        payment.setType(TransactionType.PAYMENT);
        payment.setDate(LocalDateTime.now(Clock.systemUTC()));

        if (account.getTransactions() == null) {
            account.setTransactions(new ArrayList<>());
        }
        account.setBalance(account.getBalance().add(payment.getAmount()));
        account.getTransactions().add(payment);
        this.generateInterestIfSavingsAccount(account);

        accountService.saveAccount(account);
        this.notificationService.sendToNotification(account, payment);
    }

    private Transaction generateOffUsPayment(final Account account, final TransactionDTO transactionDTO) {
        Transaction payment = new Transaction();
        payment.setAmount(transactionDTO.getAmount());
        payment.setFromBank(Bank.BANK_Z);
        payment.setReference(transactionDTO.getToAccountReference());
        payment.setAccountReference(transactionDTO.getFromAccountId());
        payment.setType(TransactionType.PAYMENT);
        payment.setDate(LocalDateTime.now(Clock.systemUTC()));

        if (account.getTransactions() == null) {
            account.setTransactions(new ArrayList<>());
        }
        account.setBalance(account.getBalance().add(payment.getAmount()));
        account.getTransactions().add(payment);

        this.generateInterestIfSavingsAccount(account);
        this.notificationService.sendToNotification(account, payment);

        return transactionRepository.save(payment);
    }

    private void generateInterestIfSavingsAccount(final Account account) {
        if (this.isSavingsAccount(account)) {
            Transaction interest = this.generateInterestTransaction(account);
            account.setBalance(account.getBalance().add(interest.getAmount()));
            account.getTransactions().add(interest);
        }
    }

    private Transaction generatePaymentFeesTransaction(final Account account, final BigDecimal amount) {
        Transaction fees = new Transaction();
        fees.setAmount(amount.multiply(TRANSACTION_FEES_PERCENT).negate());
        fees.setFromBank(Bank.BANK_X);
        fees.setReference("Transaction Fees");
        fees.setAccountReference(account.getId());
        fees.setType(TransactionType.TRANSACTION_FEES);
        fees.setDate(LocalDateTime.now(Clock.systemUTC()));

        return fees;
    }

    private Transaction generateInterestTransaction(final Account account) {
        Transaction interest = new Transaction();
        interest.setAmount(account.getBalance().multiply(INTEREST_ACCRUAL_PERCENT));
        interest.setFromBank(Bank.BANK_X);
        interest.setReference("Interest Accrued");
        interest.setAccountReference(account.getId());
        interest.setType(TransactionType.INTEREST);
        interest.setDate(LocalDateTime.now(Clock.systemUTC()));

        return interest;
    }
}
