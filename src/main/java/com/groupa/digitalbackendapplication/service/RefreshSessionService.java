package com.groupa.digitalbackendapplication.service;


import com.groupa.digitalbackendapplication.domain.entities.User;

import java.util.UUID;

public interface RefreshSessionService {
    void createLoginSession(String sessionId, UUID userId, String refreshToken);
    void invalidateLoginSession(UUID userId);
    User validate(String token);
}
