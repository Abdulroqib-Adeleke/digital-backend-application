package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.ForgetPasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminCreationResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.*;
import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.ActionType;
import com.groupa.digitalbackendapplication.domain.dto.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.Response;
import com.groupa.digitalbackendapplication.domain.enums.PersonalAccountType;
import com.groupa.digitalbackendapplication.exceptions.AccessDeniedException;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.AdminRepository;
import com.groupa.digitalbackendapplication.repository.AuditLogRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.security.CustomUserDetailsService;
import com.groupa.digitalbackendapplication.security.TokenService;
import com.groupa.digitalbackendapplication.service.AdminService;
import com.groupa.digitalbackendapplication.service.AuthService;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import com.groupa.digitalbackendapplication.service.RefreshSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshSessionService refreshSessionService;
    private final LoginSessionService loginSessionService;
    private final AdminRepository adminRepository;
    private final AdminService adminService;
    private final EmailService emailService;
    private final AccountRepository accountRepository;

    private final AuditLogRepository auditLogRepository;

    @Override
    public ResponseWrapper<AdminCreationResponse> createSystemAdmin(AdminCreationRequest payload) {
        return adminService.createSYSAdmin(payload);
    }

    @Override
    public Response<LoginResponse> loginUser(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        AuthUser authUser = (AuthUser) customUserDetailsService.loadUserByUsername(email);

        Account account = accountRepository.findByCustomerIdAndPersonalAccountType(authUser.getUser().getId(), PersonalAccountType.SAVINGS)
                .orElseThrow(()-> new BadRequestException("Something went wrong"));

        if (!passwordEncoder.matches(password, authUser.getPassword())) {
            throw new BadRequestException("Password does not match");
        }
        if(account.getAccountStatus() == AccountStatus.PENDING_VERIFICATION)
            throw new BadRequestException("Please verify account before logging in");
        if(account.getAccountStatus() == AccountStatus.FROZEN)
            throw new BadRequestException("Account suspended, contact admin to rectify");

        Response<LoginResponse> response = getLoginResponse(loginRequest, authUser.getUser());

        Customer customer = customerRepository.findById(authUser.getUser().getId()).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(customer.getEmail())
                .subject("New Login to your POI-BANK Digital Bank Account")
                .messageBody(
                        "Dear " + customer.getFirstName() + ",\n\n" +
                        "A successful login to your POI-BANK Digital Banking account " +
                        "was detected.\n\n" +
                        "If this was you, no action is required.\n\n" +
                        "If you did not perform this login, please contact " +
                        "our support team immediately.\n\n " +
                        "Regards,\n" +
                        "POI-BANK TEAM"
                )
                .build();
        emailService.sendEmail(emailDetails);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_LOGIN)
                        .userId(authUser.getUser().getId())
                        .userEmail(email)
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("customer")
                        .build());

        return response;
    }

    @Override
    public Response<LoginResponse> loginAdmin(LoginRequest payload, String adminId) {
        Admin admin = adminRepository.findByAdminIdAndEmail(adminId, payload.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        AuthUser authUser = (AuthUser) customUserDetailsService.loadUserByUsername(payload.getEmail());

        if(!admin.isActive())
            throw new BadRequestException("Admin has been suspended");

        if (!passwordEncoder.matches(payload.getPassword(), authUser.getPassword())) {
            throw new BadRequestException("Password does not match");
        }

        Response<LoginResponse> response = getLoginResponse(payload, authUser.getUser());


        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(admin.getEmail())
                .subject("New Login to your POI-BANK Digital Bank Account")
                .messageBody(
                        "Dear " + admin.getFirstName() + ",\n\n" +
                                "A successful login to your POI-BANK Digital Banking account " +
                                "was detected.\n\n" +
                                "If this was you, no action is required.\n\n" +
                                "If you did not perform this login, please contact " +
                                "our support team immediately.\n\n " +
                                "Regards,\n" +
                                "POI-BANK TEAM"
                )
                .build();
        emailService.sendEmail(emailDetails);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ADMIN_REGISTRATION)
                        .userId(authUser.getUser().getId())
                        .userEmail(admin.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return response;
    }

    public Response<LoginResponse> getNewAccessToken(String refreshToken) {

        User user = refreshSessionService.validate(refreshToken);

        Response<LoginResponse> response = buildLoginResponse(user, user.getEmail());

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ANOTHER_ACCESS_TOKEN)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("user")
                        .build());

        return response;
    }


    @Override
    public ResponseWrapper<String> forgetCustomerPassword(ForgetPasswordRequest payload) {
        Customer customer = customerRepository.findByEmail(payload.email())
                .orElseThrow(()-> new ResourceNotFoundException("user not found"));

        if(!payload.newPassword().equals(payload.confirmPassword()))
            throw new BadRequestException("Confirm password must be same as new password");

        customer.setPassword(passwordEncoder.encode(payload.confirmPassword()));
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.PASSWORD_CHANGED)
                        .userId(customer.getId())
                        .userEmail(customer.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("customer")
                        .build());

        return ResponseWrapper.<String>builder()
                .message("Password reset successful")
                .statusCode(HttpStatus.ACCEPTED)
                .build();
    }

    @Override
    public ResponseWrapper<String> forgetAdminPassword(ForgetPasswordRequest payload, String adminId) {
        Admin admin = adminRepository.findByAdminIdAndEmail(adminId, payload.email())
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        if(!payload.newPassword().equals(payload.confirmPassword()))
            throw new BadRequestException("Confirm password must be same as new password");

        admin.setPassword(passwordEncoder.encode(payload.confirmPassword()));
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.PASSWORD_CHANGED)
                        .userId(admin.getId())
                        .userEmail(admin.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return ResponseWrapper.<String>builder()
                .message("Password reset successful")
                .statusCode(HttpStatus.ACCEPTED)
                .build();
    }

    private Response<LoginResponse> getLoginResponse(LoginRequest payload, User user) {
        
        try {

            return buildLoginResponse(user, payload.getEmail());

        }catch (BadCredentialsException ex){
            log.error("Error occurred: ",ex);
            throw new AccessDeniedException("Invalid authentication credentials");
        }
    }

    private Response<LoginResponse> buildLoginResponse(User user, String email){

        String sessionId = LocalDateTime.now().toString();
        String accessToken = tokenService.generateToken(email, user.getRole(), user.getId(), sessionId);

        String refreshToken = tokenService.generateRefreshToken();
        refreshSessionService.createLoginSession(sessionId, user.getId(),refreshToken);

        loginSessionService.saveLoginSession(user.getId(), sessionId);

        LoginResponse  response = new LoginResponse(accessToken, refreshToken);

        return Response.<LoginResponse>builder()
                .data(response)
                .message("Login Successful")
                .statusCode(HttpStatus.OK.value())
                .build();
    }
}
