package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.response.CustomerSessionResponse;
import com.buffetrestaurant.service.SessionContextProvider.SessionContextSnapshot;

/** Active customer credential verification; successful results are non-null and match the requested session. */
public interface CustomerSessionVerifier {
    CustomerSessionResponse requireContext(String credential);
    SessionContextSnapshot requireSession(Long sessionId, String credential);
    /** Called within the order transaction; locks/rechecks the session and rejects bill-requested sessions. */
    SessionContextSnapshot requireSessionForOrder(Long sessionId, String credential);
}
