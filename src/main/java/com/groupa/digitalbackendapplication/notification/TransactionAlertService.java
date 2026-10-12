package com.groupa.digitalbackendapplication.notification;

import com.groupa.digitalbackendapplication.domain.entities.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public interface TransactionAlertService {
    void sendDebitAlert(Account account, Account destinationAccount, BigDecimal amount, LocalDateTime now);
    void sendCreditAlert(Account account, Account sourceAccount, BigDecimal amount, LocalDateTime now);
    void sendCreaditAlertForDeposit(Account account, BigDecimal amount, LocalDateTime now);
    void sendTransactionDeclinedAlert(Account account, BigDecimal amount, LocalDateTime now);
}
