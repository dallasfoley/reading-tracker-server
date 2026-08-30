package com.dtf.reading_tracker_server.userbook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    @EntityGraph(attributePaths = "book")
    Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    @EntityGraph(attributePaths = "book")
    List<UserBook> findAllByUserId(Long userId);
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
