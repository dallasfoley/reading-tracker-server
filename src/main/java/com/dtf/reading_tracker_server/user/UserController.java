package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        String auth0id = jwt.getSubject();
        return ResponseEntity.ok(userService.getUserByAuth0Id(auth0id));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        String auth0id = jwt.getSubject();
        userService.deleteUserByAuth0Id(auth0id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

}