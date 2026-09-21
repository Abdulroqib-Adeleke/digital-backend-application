package com.groupa.digitalbackendapplication.domain.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatementLine(LocalDateTime date, String description, String detail,
                            String type, boolean credit, BigDecimal amount, BigDecimal balanceAfter) {}