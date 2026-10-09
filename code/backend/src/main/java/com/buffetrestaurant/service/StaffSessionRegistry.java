package com.buffetrestaurant.service;

import com.buffetrestaurant.common.UserSessionKeys;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Tracks live staff HTTP sessions per account so closing or disabling an account can invalidate
 * the sessions it already holds. Sessions are held in server memory, so this matches their lifetime.
 */
@Component
public class StaffSessionRegistry implements HttpSessionListener {
    private final Map<Long, Set<HttpSession>> sessionsByUser = new ConcurrentHashMap<>();

    public synchronized void register(Long userId, HttpSession session) {
        unregister(session);
        sessionsByUser.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /** Invalidates every live session of the account; sessions already gone are ignored. */
    public void revoke(Long userId) {
        List<HttpSession> sessions;
        synchronized (this) {
            Set<HttpSession> removed = sessionsByUser.remove(userId);
            if (removed == null) return;
            sessions = new ArrayList<>(removed);
        }
        // Invalidate outside the lock: the container may call sessionDestroyed while holding its session lock.
        for (HttpSession session : sessions) {
            try {
                session.removeAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY);
                session.invalidate();
            } catch (IllegalStateException alreadyInvalidated) {
                // The session ended on its own; nothing left to revoke.
            }
        }
    }

    @Override
    public synchronized void sessionDestroyed(HttpSessionEvent event) {
        unregister(event.getSession());
    }

    private void unregister(HttpSession session) {
        sessionsByUser.values().forEach(sessions -> sessions.remove(session));
        sessionsByUser.values().removeIf(Set::isEmpty);
    }

    public synchronized int activeSessionCount(Long userId) {
        Set<HttpSession> sessions = sessionsByUser.get(userId);
        return sessions == null ? 0 : sessions.size();
    }

}
