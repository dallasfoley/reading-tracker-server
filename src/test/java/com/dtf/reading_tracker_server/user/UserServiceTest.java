package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.shared.exception.ResourceNotFoundException;
import com.dtf.reading_tracker_server.user.dto.UserResponse;
import com.dtf.reading_tracker_server.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserReturnsUserResponseWhenUserExists() {
        User user = User.builder()
                .id(42L)
                .authId("auth0|42")
                .email("reader@example.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.parse("2026-01-01T12:00:00"))
                .build();

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(42L);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.email()).isEqualTo("reader@example.com");
    }

    @Test
    void getUserThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteUserDeletesExistingUser() {
        when(userRepository.existsById(42L)).thenReturn(true);

        userService.deleteUser(42L);

        verify(userRepository).deleteById(42L);
    }

    @Test
    void deleteUserThrowsWhenUserDoesNotExist() {
        when(userRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
        verify(userRepository, never()).deleteById(404L);
    }
}
