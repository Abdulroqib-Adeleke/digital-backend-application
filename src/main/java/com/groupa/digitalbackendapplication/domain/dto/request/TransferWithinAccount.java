package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferWithinAccount(

        @NotNull(message = "Source account required")
        String sourceAccount,

        @NotNull(message = "amount is required")
        BigDecimal amount,

        @NotNull(message = "destinationAccount is required")
        String destinationAccount
) {
}
