package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.user.dto.UserResponse;

public interface UserService {
    UserResponse getUser(Long id);
    void deleteUser(Long id);
}