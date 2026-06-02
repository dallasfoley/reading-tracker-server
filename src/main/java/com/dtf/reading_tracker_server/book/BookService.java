package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;

public interface BookService {
    BookResponse search(String query);
    BookResponse get(Long id);
}
