package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.userbook.dto.UserBookRequest;
import com.dtf.reading_tracker_server.userbook.dto.UserBookResponse;

import java.util.List;

public interface UserBookService {
    UserBookResponse getByUserAndBook(Long userId, Long bookId);
    List<UserBookResponse> getAllByUser(Long userId);
    UserBookResponse create(Long userId, UserBookRequest request);
    void delete(Long userId, Long bookId);
    UserBookResponse updateStatus(Long userId, Long bookId, String status);
    UserBookResponse updateCurrentPage(Long userId, Long bookId, Integer currentPage);
    UserBookResponse updateRating(Long userId, Long bookId, Integer rating);
}
