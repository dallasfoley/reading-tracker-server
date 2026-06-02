package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.user.dto.UserResponse;

public interface UserService {
    public UserResponse getUserByAuth0Id(String auth0id);
    public void deleteUserByAuth0Id(String auth0id);
}