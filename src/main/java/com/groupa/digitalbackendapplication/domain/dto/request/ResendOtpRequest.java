package com.groupa.digitalbackendapplication.domain.dto.request;

import com.groupa.digitalbackendapplication.domain.enums.VerificationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ResendOtpRequest {
    @NotNull(message = "Email cannot be null")
    private String email;

    private VerificationType verificationType;
}
