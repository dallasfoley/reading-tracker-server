package com.dtf.reading_tracker_server.book;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByOpenLibraryKey(String openLibraryKey);
}
