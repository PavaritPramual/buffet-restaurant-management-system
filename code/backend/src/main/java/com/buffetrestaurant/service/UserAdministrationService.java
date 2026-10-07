package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.response.UserResponse;
import java.util.List;

/** User administration currently supports creation and listing only. */
public interface UserAdministrationService {
    UserResponse createUser(CreateUserRequest request);
    List<UserResponse> listUsers();
}
