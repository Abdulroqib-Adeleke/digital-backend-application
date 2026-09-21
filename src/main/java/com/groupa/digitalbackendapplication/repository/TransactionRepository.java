package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("select t from Transaction t where t.destinationAccount = :account")
    List<Transaction> findAllByDestinationAccount(Account account);

    @Query("select t from Transaction t where t.sourceAccount = :account or t.destinationAccount = :account order by t.createdAt desc")
    List<Transaction> findAllByAccount(Account account);

    @Override
    Optional<Transaction> findById(UUID id);

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount = :account or t.destinationAccount = :account " +
            "AND t.createdAt >= :startDate " +
            "AND t.createdAt <= :endDate or t.updatedAt <= :endDate " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsByAccountAndDateRange(
            @Param("account") Account account,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
