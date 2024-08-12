package com.ctwi.service;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class SessionManager {

    private static final Map<String, String> sessions = new HashMap<>();

    public static String createSession(String email) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, email);
        return sessionId;
    }

    public static boolean isValidSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    private static String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    public static String getEmailBySessionId(String sessionId) {
        return sessions.get(sessionId);
    }

    public static void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
