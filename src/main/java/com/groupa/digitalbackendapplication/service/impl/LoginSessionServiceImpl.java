package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.entities.LoginSession;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.repository.LoginSessionRepository;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginSessionServiceImpl implements LoginSessionService {

    private final LoginSessionRepository loginSessionRepository;

    @Override
    public void saveLoginSession(UUID userId, String sessionId) {
        Optional<LoginSession> loginSessionOptional = loginSessionRepository.findByUserId(userId);
        if (loginSessionOptional.isPresent()) {
            LoginSession loginSession = loginSessionOptional.get();
            loginSession.setActiveSessionId(sessionId);
            loginSession.setLoggedIn(Boolean.TRUE);
            loginSession.setTimeOfLogin(LocalDateTime.now());
            loginSessionRepository.save(loginSession);
        } else {
            LoginSession newLoginSession = LoginSession.builder()
                    .loggedIn(Boolean.TRUE)
                    .userId(userId)
                    .activeSessionId(sessionId)
                    .timeOfLogin(LocalDateTime.now())
                    .build();
            loginSessionRepository.save(newLoginSession);
        }
    }

    @Override
    public void invalidateLoginSession(UUID userId) {

        Optional<LoginSession> loginSessionOptional = loginSessionRepository.findByUserId(userId);
        loginSessionOptional.ifPresent(loginSessionRepository::delete);
    }

    @Override
    public Optional<LoginSession> getLoginSession(UUID userId) {

        return loginSessionRepository.findByUserId(userId);
    }

    @Override
    public String getActiveSessionId(UUID userId) {
        LoginSession loginSessionOptional = loginSessionRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Something went Wrong"));

        return loginSessionOptional.getActiveSessionId();
    }
}
