package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.helper.SHA_Hash;
import com.groupa.digitalbackendapplication.domain.entities.RefreshSession;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.exceptions.InvalidTokenException;
import com.groupa.digitalbackendapplication.repository.RefreshSessionRepository;
import com.groupa.digitalbackendapplication.repository.UserRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.RefreshSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshSessionServiceImpl implements RefreshSessionService {

    private final RefreshSessionRepository refreshSessionRepository;
    private final SHA_Hash shaHash;
    private final UserRepository userRepository;

    @Value("${jwt.refresh.expiration.time}")
    private long refreshTokenExpirationTime;

    @Transactional
    @Override
    public void createLoginSession(String sessionId, UUID userId, String refreshToken) {
        LocalDateTime expiration =  LocalDateTime.now().plusDays(refreshTokenExpirationTime);

        String hashedRefreshToken = shaHash.hashToken(refreshToken);

        Optional<RefreshSession> activeSessionOptional = refreshSessionRepository.findByUserId(userId);
        if (activeSessionOptional.isEmpty()){
            RefreshSession newUserSession = RefreshSession.builder()
                    .activeSessionId(sessionId)
                    .token(hashedRefreshToken)
                    .userId(userId)
                    .expiryDate(expiration)
                    .build();
            refreshSessionRepository.save(newUserSession);
        }else{
            RefreshSession activeSession = activeSessionOptional.get();
            activeSession.setActiveSessionId(sessionId);
            activeSession.setToken(hashedRefreshToken);
            activeSession.setExpiryDate(expiration);
            refreshSessionRepository.save(activeSession);
        }
    }

    @Transactional
    @Override
    public void invalidateLoginSession(UUID userId) {
        Optional<RefreshSession> activeSessionOptional = refreshSessionRepository.findByUserId(userId);
        activeSessionOptional.ifPresent(refreshSessionRepository::delete);
    }

    @Override
    public User validate(String token) {

        String hashedRefreshToken = shaHash.hashToken(token);

        RefreshSession activeSession = refreshSessionRepository.findByToken(hashedRefreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (activeSession.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshSessionRepository.delete(activeSession);
            throw new InvalidTokenException("Refresh token expired");
        }

        return userRepository.findById(activeSession.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User no longer exists"));
    }
}
