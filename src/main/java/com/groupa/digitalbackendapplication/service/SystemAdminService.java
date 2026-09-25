package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.AdminSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminDto;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TotalAdminOverview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SystemAdminService {

    ResponseWrapper<AdminDto> getAdmin(String adminId);
    ResponseWrapper<TotalAdminOverview> totalAdminOverview();
    ResponseWrapper<Page<AdminDto>> getAllAdmin(Pageable pageable);
    ResponseWrapper<String> suspendAdmin(AdminSuspensionRequest payload);
    ResponseWrapper<String> reactivateAdmin(String adminId);
    ResponseWrapper<String> deleteAdminAccount(String adminId);
}
