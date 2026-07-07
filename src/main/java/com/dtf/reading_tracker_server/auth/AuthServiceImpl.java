package com.dtf.reading_tracker_server.auth;

import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.user.UserRepository;
import com.dtf.reading_tracker_server.user.dto.UserResponse;
import com.dtf.reading_tracker_server.user.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;

    public UserResponse provisionUser(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        }

        String auth0Id = jwt.getSubject(); // e.g. "google-oauth2|123456"
        String email = jwt.getClaimAsString("email");

        if (email == null) {
            email = auth0Id + "@placeholder.local"; // fallback for testing
        }

        String finalEmail = email;
        return userRepository.findByAuthId(auth0Id)
                .map(UserResponse::from)                    // existing user, just return
                .orElseGet(() -> {                          // first login, create them
                    User newUser = User.builder().authId(auth0Id)
                            .email(finalEmail)
                            .role(Role.USER)
                            .build();
                    return UserResponse.from(userRepository.save(newUser));
                });
    }
}
