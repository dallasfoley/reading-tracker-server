package com.dtf.reading_tracker_server.userbook.dto;

import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;

public record UserBookRequest(
        Long bookId,
        String openLibraryKey,
        ReadingStatus status,    // e.g. WANT_TO_READ, READING, READ
        Integer userRating,
        Integer currentPage
) {}
