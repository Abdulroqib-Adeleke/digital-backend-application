package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AccountDailyAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId")
    private Account account;

    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private LocalDate date;

    public AccountDailyAudit(Account account, BigDecimal openingAmount, BigDecimal closingAmount, LocalDate date) {
        this.account = account;
        this.openingAmount = openingAmount;
        this.closingAmount = closingAmount;
        this.date = date;
    }
}
