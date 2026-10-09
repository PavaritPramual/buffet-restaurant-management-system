package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.response.UserContext;

/** Authenticates credentials; success returns a complete non-null identity. */
public interface AuthenticationService {
    UserContext authenticate(String username, String password);

    /** True while the account exists, is enabled and has not been closed; used to re-check a fresh login. */
    boolean isLoginAllowed(Long userId);
}
