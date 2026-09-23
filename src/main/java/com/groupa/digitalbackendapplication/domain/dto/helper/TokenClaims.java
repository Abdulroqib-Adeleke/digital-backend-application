package com.groupa.digitalbackendapplication.domain.dto.helper;

import com.groupa.digitalbackendapplication.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenClaims {

    private String username;
    private String userSessionId;
    private String tenancyId;
    private Role role;

}
