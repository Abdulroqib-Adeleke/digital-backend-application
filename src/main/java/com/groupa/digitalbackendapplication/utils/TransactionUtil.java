package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;

import java.math.BigDecimal;

public class TransactionUtil {

    public static Transaction buildTransactionEntity(TransactionType transactionType, TransactionStatus transactionStatus,
                                               Account sourceAccount, String sourceAccountName, Account destinationAccount, String destinationAccountNumber, String destinationAccountName, BigDecimal amount,
                                               String description){
        return Transaction.builder()
                .transactionType(transactionType)
                .transactionStatus(transactionStatus)
                .sourceAccount(sourceAccount)
                .accountName(sourceAccountName)
                .destinationAccount(destinationAccount)
                .destinationAccountNumber(destinationAccountNumber)
                .destinationAccountName(destinationAccountName)
                .amountTransferred(amount)
                .description(description)
                .build();
    }
}
