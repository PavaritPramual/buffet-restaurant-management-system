package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.response.UserContext;

/** Authenticates credentials; success returns a complete non-null identity. */
public interface AuthenticationService {
    UserContext authenticate(String username, String password);
}
