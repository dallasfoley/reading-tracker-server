package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.userbook.dto.UserBookRequest;
import com.dtf.reading_tracker_server.userbook.dto.UserBookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBookServiceImpl implements UserBookService{

    private final UserBookRepository userBookRepository;

    @Override
    public UserBookResponse getByUserAndBook(Long userId, Long bookId) {
        return null;
    }

    @Override
    public List<UserBookResponse> getAllByUser(Long userId) {
        return List.of();
    }

    @Override
    public UserBookResponse create(Long userId, UserBookRequest request) {
        return null;
    }

    @Override
    public void delete(Long userId, Long bookId) {

    }

    @Override
    public UserBookResponse updateStatus(Long userId, Long bookId, String status) {
        return null;
    }

    @Override
    public UserBookResponse updateCurrentPage(Long userId, Long bookId, int currentPage) {
        return null;
    }

    @Override
    public UserBookResponse updateRating(Long userId, Long bookId, int rating) {
        return null;
    }
}
