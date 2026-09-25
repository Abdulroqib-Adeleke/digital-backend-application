package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.response.DailyTransactionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.service.impl.DailyTransactionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'SYS_ADMIN')")
@RequestMapping("/api/daily-transactions")
public class DailyTransactionController {

    private final DailyTransactionServiceImpl dailyTransactionService;

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/daily")
    public ResponseEntity<ResponseWrapper<DailyTransactionResponse>> getDailyTransactionsSummary(){
        return ResponseEntity.ok(dailyTransactionService.getDailyTransactionSummary());
    }
}
