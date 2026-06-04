package com.dtf.reading_tracker_server.userbook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    List<UserBook> findAllByUserId(Long userId);
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
