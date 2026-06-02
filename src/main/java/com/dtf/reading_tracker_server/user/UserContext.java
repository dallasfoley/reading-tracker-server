package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserRepository userRepository;

    /** Returns the full User entity for the currently authenticated principal. */
    public User getCurrentUser(Jwt jwt) {
        String auth0id = jwt.getSubject();
        return userRepository.findByAuthId(auth0id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    /** Convenience method to get just the internal user ID. */
    public Long getCurrentUserId(Jwt jwt) {
        return getCurrentUser(jwt).getId();
    }

    /** Convenience method to get the Auth0 subject if you ever need it directly. */
    public String getAuth0Id(Jwt jwt) {
        return jwt.getSubject();
    }
}