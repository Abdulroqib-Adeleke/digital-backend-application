package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateStatementRequest(@NotNull String accountNumber,
                                       @NotNull LocalDate from,
                                       @NotNull LocalDate to) {
}
