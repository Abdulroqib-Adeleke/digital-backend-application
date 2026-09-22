package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.AccountDailyAudit;
import com.groupa.digitalbackendapplication.domain.entities.DailyTransferTotal;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;
import com.groupa.digitalbackendapplication.repository.AccountDailyAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountDailyAuditUtil {

    private final AccountDailyAuditRepository  accountDailyAuditRepository;


    public void initializeDailyAuditIfNotExist(Account account) {
        Optional<AccountDailyAudit> audit = accountDailyAuditRepository.findByAccountIdAndDate(account.getId(), LocalDate.now());
        if(audit.isEmpty()){
            accountDailyAuditRepository.save(new AccountDailyAudit(account, account.getBalance(), account.getBalance(),
                    LocalDate.now()));
        }
    }

    public void recordDailyAccountAudit(Account account, TransactionType transactionType, BigDecimal amount) {

        Optional<AccountDailyAudit> accountLastAuditOptional = accountDailyAuditRepository
                .findFirstByAccountIdOrderByDateDesc(account.getId());

        AccountDailyAudit accountLastDailyAudit = accountLastAuditOptional.orElseGet(() ->
                new AccountDailyAudit(account, account.getBalance(), account.getBalance(),
                LocalDate.now()));

        AccountDailyAudit dailyAudit = accountDailyAuditRepository
                .findForUpdate(account, LocalDate.now()).orElseGet(()->
                        new AccountDailyAudit(account, accountLastDailyAudit.getClosingAmount(),
                                accountLastDailyAudit.getClosingAmount(), LocalDate.now()));

        BigDecimal newClosing =  dailyAudit.getClosingAmount();

        if(dailyAudit.getClosingAmount().compareTo(BigDecimal.ZERO) > 0) {}

        if(transactionType.equals(TransactionType.DEPOSIT) || transactionType.equals(TransactionType.TRANSFER)){
            newClosing = newClosing.add(amount);
        }else if(transactionType.equals(TransactionType.WITHDRAWAL)){
            newClosing = newClosing.subtract(amount);
        }
        dailyAudit.setClosingAmount(newClosing);

        accountDailyAuditRepository.save(dailyAudit);
    }

}
