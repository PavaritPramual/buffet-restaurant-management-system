package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.request.UpdateUserProfileRequest;
import com.buffetrestaurant.dto.response.UserResponse;
import java.util.List;

/** User administration supports creation, listing and profile contact updates. */
public interface UserAdministrationService {
    UserResponse createUser(CreateUserRequest request);
    List<UserResponse> listUsers();
    UserResponse updateProfile(Long userId, UpdateUserProfileRequest request);
}
