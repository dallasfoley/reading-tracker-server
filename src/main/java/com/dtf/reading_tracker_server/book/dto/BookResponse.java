package com.dtf.reading_tracker_server.book.dto;

import com.dtf.reading_tracker_server.book.Book;

public record BookResponse(
        Long id,
        String openLibraryKey,
        String title,
        String author,
        Integer yearPublished,
        String genre,
        Integer pageCount,
        String coverUrl,
        String description
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getOpenLibraryKey(),
                book.getTitle(),
                book.getAuthor(),
                book.getYearPublished(),
                book.getGenre(),
                book.getPageCount(),
                book.getCoverUrl(),
                book.getDescription()
        );
    }
}
