package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.user.dto.UserResponse;

public interface UserService {
    public UserResponse getUser(Long id);
    public void deleteUser(Long id);
}