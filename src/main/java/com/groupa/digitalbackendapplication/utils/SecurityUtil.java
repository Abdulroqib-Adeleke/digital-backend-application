package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.dto.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.Response;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.ActionType;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.repository.AuditLogRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import com.groupa.digitalbackendapplication.service.RefreshSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final LoginSessionService loginSessionService;
    private final RefreshSessionService refreshSessionService;
    private final AuditLogRepository auditLogRepository;

    public AuthUser getSecurityPrincipal(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new BadRequestException("User profile not available");

        return (AuthUser) authentication.getPrincipal();
    }

    public ResponseWrapper<LogoutResponse> logout() {
        AuthUser authUser = getSecurityPrincipal();
        User user = authUser.getUser();
        loginSessionService.invalidateLoginSession(user.getId());

        refreshSessionService.invalidateLoginSession(user.getId());

        LogoutResponse logoutResponse = new LogoutResponse("Logout Successful");

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_LOGOUT)
                        .userId(user.getId())
                        .userEmail(authUser.getUser().getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("user")
                        .build());

        return ResponseWrapper.<LogoutResponse>builder()
                .message("Success")
                .data(logoutResponse)
                .statusCode(HttpStatus.OK)
                .build();
    }
}
