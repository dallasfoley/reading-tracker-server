package com.dtf.reading_tracker_server.auth;

import com.dtf.reading_tracker_server.user.dto.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {
    UserResponse provisionUser(Jwt jwt);
}
