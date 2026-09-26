package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.OpenDiningSessionRequest;
import com.buffetrestaurant.dto.response.DiningSessionResponse;

public interface DiningSessionService {
    DiningSessionResponse openSession(OpenDiningSessionRequest request);
    DiningSessionResponse getActiveSessionByToken(String token);
    DiningSessionResponse closeSession(Long sessionId);
}
