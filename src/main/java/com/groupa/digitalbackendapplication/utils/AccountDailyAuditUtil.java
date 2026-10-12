package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.AccountDailyAudit;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;
import com.groupa.digitalbackendapplication.repository.AccountDailyAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;


@Component
@RequiredArgsConstructor
public class AccountDailyAuditUtil {


    private final AccountDailyAuditRepository accountDailyAuditRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void initializeDailyAuditIfNotExist(Account account) {
        LocalDate today = LocalDate.now();
        accountDailyAuditRepository.findForUpdate(account, today)
                .orElseGet(() -> createForToday(account, today));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordDailyAccountAudit(Account account, TransactionType type, BigDecimal amount) {
        LocalDate today = LocalDate.now();

        AccountDailyAudit audit = accountDailyAuditRepository.findForUpdate(account, today)
                .orElseGet(() -> createForToday(account, today));

        BigDecimal delta = switch (type) {
            case DEPOSIT, TRANSFER -> amount;
            case WITHDRAWAL -> amount.negate();
        };

        audit.setClosingAmount(audit.getClosingAmount().add(delta));
        accountDailyAuditRepository.save(audit);
    }

    private AccountDailyAudit createForToday(Account account, LocalDate today) {

        BigDecimal opening = accountDailyAuditRepository
                .findFirstByAccountIdOrderByDateDesc(account.getId())
                .map(AccountDailyAudit::getClosingAmount)
                .orElse(account.getBalance());

        return accountDailyAuditRepository.save(new AccountDailyAudit(account, opening, opening, today));
    }
}
