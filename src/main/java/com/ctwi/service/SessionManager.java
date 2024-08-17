package com.ctwi.service;


import com.ctwi.auth.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SessionManager {
    private AuthRepository authRepo;

    @Autowired
    public SessionManager(AuthRepository authRepo) {
        this.authRepo = authRepo;
    }

    private final Map<String, String> sessions = new HashMap<>();

    public String createSession(String email) {
        return authRepo.createSession(email);
    }

    public boolean isValidSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    public String getEmailBySessionId(String sessionId) {
        return sessions.get(sessionId);
    }

    public void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
