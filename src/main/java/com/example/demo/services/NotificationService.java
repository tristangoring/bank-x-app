package com.example.demo.services;

import com.example.demo.entities.Account;
import com.example.demo.entities.Transaction;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final String NOTIFICATION_FROM_FORMAT = "Transaction on Account [%s]\n%s made to account [%s]:\nAmount: %.2f.\nReference: %s\nNew balance: %.2f\n";
    private final String NOTIFICATION_TO_FORMAT = "Transaction on Account [%s]\n%s received from account [%s]:\nAmount: %.2f.\nReference: %s\nNew balance: %.2f\n";

    public void sendFromNotification(Account account, Transaction transaction) {
        // TODO: Email / SMS logic & integration on notification preference
        System.out.println(String.format(NOTIFICATION_FROM_FORMAT, account.getId(), transaction.getType(), transaction.getAccountReference(), transaction.getAmount(), transaction.getReference(), account.getBalance()));
    }

    public void sendToNotification(Account account, Transaction transaction) {
        // TODO: Email / SMS logic & integration on notification preference
        System.out.println(String.format(NOTIFICATION_TO_FORMAT, account.getId(), transaction.getType(), transaction.getAccountReference(), transaction.getAmount(), transaction.getReference(), account.getBalance()));
    }
}
