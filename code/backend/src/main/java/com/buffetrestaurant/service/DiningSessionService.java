package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.OpenDiningSessionRequest;
import com.buffetrestaurant.dto.response.DiningSessionResponse;
import java.util.List;

public interface DiningSessionService {
    DiningSessionResponse openSession(OpenDiningSessionRequest request);
    List<DiningSessionResponse> getActiveSessions();
    DiningSessionResponse getSession(Long sessionId);
    DiningSessionResponse getActiveSessionByToken(String token);
    DiningSessionResponse closeSession(Long sessionId);
}
