package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.ChangePasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.SecondaryAccountCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AccountCreatedResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.CustomerDto;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.Response;
import com.groupa.digitalbackendapplication.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CUSTOMER')")
public class AccountController {

    private final CustomerService customerService;

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/accountName/{account-number}")
    public ResponseWrapper<String> getAccountNameByAccountNumber(@PathVariable("account-number") String accountNumber) {
        return customerService.getUserNameByAccountNumber(accountNumber);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create-secondary-account")
    public ResponseWrapper<AccountCreatedResponse> createOtherAccount(@RequestBody @Valid SecondaryAccountCreationRequest payload) {
        return customerService.createOtherAccount(payload);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/user-profile")
    public Response<CustomerDto> getUserProfile() {
        return customerService.getUserProfile();
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/password-reset")
    public ResponseWrapper<String> changePassword(@Valid ChangePasswordRequest payload){
        return customerService.changePassword(payload);
    }
}
