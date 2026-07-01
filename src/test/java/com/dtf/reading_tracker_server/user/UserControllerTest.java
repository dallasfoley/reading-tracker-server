package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.shared.config.CorsConfig;
import com.dtf.reading_tracker_server.shared.config.SecurityConfig;
import com.dtf.reading_tracker_server.user.enums.Role;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserContext userContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getMeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMeReturnsCurrentUserFromJwtSubject() throws Exception {
        User user = User.builder()
                .id(9L)
                .authId("auth0|me")
                .email("me@example.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.parse("2026-01-01T12:00:00"))
                .build();

        when(userContext.getCurrentUser(any())).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                        .with(jwt().jwt(token -> token.subject("auth0|me"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.email").value("me@example.com"));
    }

    @Test
    void deleteMeDeletesCurrentUser() throws Exception {
        when(userContext.getCurrentUserId(any())).thenReturn(9L);

        mockMvc.perform(delete("/api/users/me")
                        .with(jwt().jwt(token -> token.subject("auth0|me"))))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(9L);
    }
}
