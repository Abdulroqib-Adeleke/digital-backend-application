package com.groupa.digitalbackendapplication.domain.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatementData(String accountName, String accountNumber, String accountType,
                            LocalDate from, LocalDate to, BigDecimal openingBalance,
                            BigDecimal closingBalance, List<StatementLine> lines) {}
