package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminSuspensionRequest(
        @NotNull(message = "Admin id cannot be blank")
        String adminId,

        @NotBlank(message = "reason cannot be blank")
        String suspensionReason
) {
}
