package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.ChangePasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.SecondaryAccountCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AccountCreatedResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.CustomerDto;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.Response;

import java.util.UUID;

public interface CustomerService {

    ResponseWrapper<AccountCreatedResponse> createPersonalAccount(CustomerRegistrationRequest payload);

    ResponseWrapper<AccountCreatedResponse> createOtherAccount(SecondaryAccountCreationRequest payload);

    Response<CustomerDto> getUserProfile();

    ResponseWrapper<String> getUserNameByAccountNumber(String accountNumber);

    Response<CustomerDto> getUserProfileById(UUID userId);

    ResponseWrapper<String> changePassword(ChangePasswordRequest payload);
}
