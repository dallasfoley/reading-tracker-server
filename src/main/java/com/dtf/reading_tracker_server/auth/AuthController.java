package com.dtf.reading_tracker_server.auth;

import com.dtf.reading_tracker_server.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/provision")
    public ResponseEntity<UserResponse> provision(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(200)
                .body(authService.provisionUser(jwt));
    }
}
