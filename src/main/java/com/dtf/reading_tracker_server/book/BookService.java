package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;

import java.util.List;

public interface BookService {
    List<BookResponse> search(String query);
    BookResponse get(Long id);
    Book findOrCreateFromOpenLibrary(String openLibraryKey);
}
