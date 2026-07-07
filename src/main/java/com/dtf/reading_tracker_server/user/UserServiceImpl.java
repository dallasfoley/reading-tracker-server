package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.shared.exception.InvalidRequestException;
import com.dtf.reading_tracker_server.shared.exception.ResourceNotFoundException;
import com.dtf.reading_tracker_server.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUser(Long id) {
        validateId(id);

        final User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    @Override
    public void deleteUser(Long id) {
        validateId(id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidRequestException("user id must be positive");
        }
    }
}
