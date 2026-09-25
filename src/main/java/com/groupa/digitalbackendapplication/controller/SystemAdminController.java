package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminCreationResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminDto;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TotalAdminOverview;
import com.groupa.digitalbackendapplication.service.AdminService;
import com.groupa.digitalbackendapplication.service.SystemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys-admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SYS_ADMIN')")
public class SystemAdminController {

    private final AdminService  adminService;
    private final SystemAdminService systemAdminService;

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create-Admin")
    public ResponseWrapper<AdminCreationResponse> createAdmin(@Valid @RequestBody AdminCreationRequest payload){
        return adminService.createAdmin(payload);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/admin-overview")
    public ResponseWrapper<TotalAdminOverview> getAdminOverview(){
        return systemAdminService.totalAdminOverview();
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/fetch-all-admin")
    public ResponseWrapper<Page<AdminDto>> getAllAdmin(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        return systemAdminService.getAllAdmin(pageable);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/fetch-admin-by-id/{admin-id}")
    public ResponseWrapper<AdminDto> getAdminById(@PathVariable("admin-id") String adminId){
        return systemAdminService.getAdmin(adminId);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/suspend-admin")
    public ResponseWrapper<String> suspendAdmin(@RequestBody @Valid AdminSuspensionRequest payload){
        return systemAdminService.suspendAdmin(payload);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/reactivate-admin")
    public ResponseWrapper<String> reactivateAdmin(@RequestParam String adminId){
        return systemAdminService.reactivateAdmin(adminId);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/delete-admin")
    public ResponseWrapper<String> deleteAdmin(@RequestParam String adminId){
        return systemAdminService.deleteAdminAccount(adminId);
    }
}
