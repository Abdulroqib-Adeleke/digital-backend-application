package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.AccountDailyAudit;
import com.groupa.digitalbackendapplication.domain.entities.DailyTransferTotal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface AccountDailyAuditRepository extends JpaRepository<AccountDailyAudit, UUID> {

    AccountDailyAudit findByAccountIdAndDate(UUID accountId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountDailyAudit a " +
            "where a.account = :account and a.date = :date")
    Optional<AccountDailyAudit> findForUpdate(@Param("account") Account accountId,
                                              @Param("date") LocalDate date);

    Optional<AccountDailyAudit> findFirstByAccountIdOrderByDateDesc(UUID accountId);
}
