package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.AdminSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminDto;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TotalAdminOverview;
import com.groupa.digitalbackendapplication.domain.entities.Admin;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.ActionType;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.repository.AdminRepository;
import com.groupa.digitalbackendapplication.repository.AuditLogRepository;
import com.groupa.digitalbackendapplication.service.SystemAdminService;
import com.groupa.digitalbackendapplication.utils.LoginSessionUtil;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemAdminServiceImpl implements SystemAdminService {

    private final AdminRepository adminRepository;
    private final AuditLogRepository auditLogRepository;
    private final SecurityUtil securityUtil;
    private final LoginSessionUtil loginSessionUtil;
    private final EmailService  emailService;

    @Override
    public ResponseWrapper<AdminDto> getAdmin(String adminId) {

        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        AdminDto dto = buildAdminDto(admin);

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_PROFILE_FETCHED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("Admin")
                        .build());

        return ResponseWrapper.<AdminDto>builder()
                .data(dto)
                .message("Fetch admin profile successfully")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<TotalAdminOverview> totalAdminOverview() {
        long totalSystemAdmin = adminRepository.countByRole(Role.SYS_ADMIN);
        long totalAdmin = adminRepository.countByRole(Role.ADMIN);
        long totalActiveAdmin = adminRepository.countByRoleAndActive(Role.ADMIN, true);
        long totalSuspendedAdmin = adminRepository.countByRoleAndActive(Role.ADMIN, false);

        TotalAdminOverview response = new TotalAdminOverview(
                totalSystemAdmin, totalAdmin, totalActiveAdmin, totalSuspendedAdmin);

        return ResponseWrapper.<TotalAdminOverview>builder()
                .data(response)
                .message("Success")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<Page<AdminDto>> getAllAdmin(Pageable pageable) {
        Page<Admin> adminPage = adminRepository.findAllByRole(Role.ADMIN, pageable);

        Page<AdminDto> adminDtoPage = adminPage.map(this::buildAdminDto);

        return ResponseWrapper.<Page<AdminDto>>builder()
                .data(adminDtoPage)
                .message("Success")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<String> suspendAdmin(AdminSuspensionRequest payload) {

        Admin admin = adminRepository.findByAdminId(payload.adminId())
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        if(!admin.isActive())
            throw new RuntimeException("Admin is not active");

        admin.setActive(false);
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        try {
            String emailSubject = "Suspension";
            String emailBody = "Dear " + admin.getFirstName() + " " + admin.getLastName().toUpperCase(Locale.ROOT) + " we regret to inform you that your access to the admin portal has been suspended " +
                    "due to:\n\n " + payload.suspensionReason();
            sendMail(admin.getEmail(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("Suspension email failed to send to {}: {}", admin.getEmail(), e.getMessage());
        }

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ACCOUNT_SUSPENDED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return ResponseWrapper.<String>builder()
                .data("Admin suspended")
                .message("Success")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<String> reactivateAdmin(String adminId) {

        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        if(admin.isActive())
            throw new RuntimeException("Admin is already active");

        admin.setActive(true);
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        try {
            String emailSubject = "Reactivation";
            String emailBody = "Dear " + admin.getFirstName() + " " + admin.getLastName().toUpperCase(Locale.ROOT) +
                    " After careful consideration we are pleased to inform you that your access to the admin portal has been reactivated.\n\n " +
                    "We look forward to your impartial commitment to your duties.\n\n " +
                    "Signed \n POI-BANK";
            sendMail(admin.getEmail(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("reactivation email failed to send to {}: {}", admin.getEmail(), e.getMessage());
        }

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ACCOUNT_UNSUSPENDED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return ResponseWrapper.<String>builder()
                .data("Admin Reactivation")
                .message("Success")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<String> deleteAdminAccount(String adminId) {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        if(admin.isActive())
            throw new BadRequestException("Admin has to have been suspended before account can be deleted");

        adminRepository.delete(admin);

        try {
            String emailSubject = "Deactivation";
            String emailBody = "Dear " + admin.getFirstName() + " " + admin.getLastName().toUpperCase(Locale.ROOT) +
                    " Your Admin account has permanently deactivated.\n\n " +
                    "We appreciate your time with our bank.\n\n " +
                    "Signed \n POI-BANK";
            sendMail(admin.getEmail(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("deactivation email failed to send to {}: {}", admin.getEmail(), e.getMessage());
        }

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ACCOUNT_DELETED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return ResponseWrapper.<String>builder()
                .data("Admin account deleted")
                .message("Success")
                .statusCode(HttpStatus.OK)
                .build();
    }

    private AdminDto buildAdminDto(Admin admin){
        return AdminDto.builder()
                .adminId(admin.getAdminId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(admin.getEmail())
                .phoneNumber(admin.getPhoneNumber())
                .gender(admin.getGender())
                .dateOfBirth(admin.getDateOfBirth())
                .role(admin.getRole())
                .address(admin.getAddress())
                .build();
    }

    private void sendMail(String email, String emailSubject, String emailBody){

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(email)
                .subject(emailSubject)
                .messageBody(emailBody)
                .build();
        emailService.sendEmail(emailDetails);
    }
}
