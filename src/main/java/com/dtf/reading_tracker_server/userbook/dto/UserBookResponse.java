package com.dtf.reading_tracker_server.userbook.dto;

import com.dtf.reading_tracker_server.userbook.UserBook;
import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;

public record UserBookResponse(
        Long id,
        ReadingStatus status,
        Integer userRating,
        Integer currentPage,
        Long bookId,
        String bookTitle,
        String bookAuthor,
        String bookCoverUrl   // enough for a thumbnail
) {
    public static UserBookResponse from(UserBook userBook) {
        var book = userBook.getBook();
        return new UserBookResponse(
                userBook.getId(),
                userBook.getStatus(),
                userBook.getUserRating(),
                userBook.getCurrentPage(),
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverUrl()
        );
    }
}