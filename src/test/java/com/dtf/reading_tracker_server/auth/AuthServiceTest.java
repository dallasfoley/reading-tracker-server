package com.dtf.reading_tracker_server.auth;

import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.user.UserRepository;
import com.dtf.reading_tracker_server.user.dto.UserResponse;
import com.dtf.reading_tracker_server.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void provisionUserReturnsExistingUserForAuth0Subject() {
        User existingUser = User.builder()
                .id(1L)
                .authId("auth0|existing")
                .email("existing@example.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.parse("2026-01-01T12:00:00"))
                .build();

        when(userRepository.findByAuthId("auth0|existing")).thenReturn(Optional.of(existingUser));

        UserResponse response = authService.provisionUser(jwt("auth0|existing", "new@example.com"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("existing@example.com");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void provisionUserCreatesUserFromJwtEmailWhenSubjectIsNew() {
        when(userRepository.findByAuthId("auth0|new")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(2L);
                    user.setCreatedAt(LocalDateTime.parse("2026-01-02T12:00:00"));
                    return user;
                });

        UserResponse response = authService.provisionUser(jwt("auth0|new", "new@example.com"));

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.email()).isEqualTo("new@example.com");

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getAuthId()).isEqualTo("auth0|new");
        assertThat(savedUser.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void provisionUserFallsBackToPlaceholderEmailWhenJwtHasNoEmail() {
        when(userRepository.findByAuthId("auth0|no-email")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.provisionUser(jwt("auth0|no-email", null));

        assertThat(response.email()).isEqualTo("auth0|no-email@placeholder.local");
    }

    private static Jwt jwt(String subject, String email) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject);

        if (email != null) {
            builder.claim("email", email);
        }

        return builder.build();
    }
}
