package com.dtf.reading_tracker_server.auth;

import com.dtf.reading_tracker_server.shared.config.CorsConfig;
import com.dtf.reading_tracker_server.shared.config.SecurityConfig;
import com.dtf.reading_tracker_server.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void provisionRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/provision"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void provisionUsesJwtPrincipalAndReturnsProvisionedUser() throws Exception {
        when(authService.provisionUser(any())).thenReturn(new UserResponse(
                7L,
                "reader@example.com",
                LocalDateTime.parse("2026-01-01T12:00:00")
        ));

        mockMvc.perform(post("/api/auth/provision")
                        .with(jwt().jwt(token -> token
                                .subject("auth0|reader")
                                .claim("email", "reader@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.email").value("reader@example.com"));

        verify(authService).provisionUser(any());
    }
}
