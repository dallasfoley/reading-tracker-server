package com.dtf.reading_tracker_server.user;

import com.dtf.reading_tracker_server.exception.ResourceNotFoundException;
import com.dtf.reading_tracker_server.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUserByAuth0Id(String auth0id) {
        User user = userRepository.findByAuthId(auth0id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    @Override
    public void deleteUserByAuth0Id(String auth0id) {
        if (!userRepository.existsByAuthId(auth0id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteByAuthId(auth0id);
    }
}