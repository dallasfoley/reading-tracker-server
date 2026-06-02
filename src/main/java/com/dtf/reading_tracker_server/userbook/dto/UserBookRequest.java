package com.dtf.reading_tracker_server.userbook.dto;

import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;

public record UserBookRequest(
        Long bookId,
        ReadingStatus status,    // e.g. WANT_TO_READ, READING, READ
        int userRating,
        int currentPage
) {}