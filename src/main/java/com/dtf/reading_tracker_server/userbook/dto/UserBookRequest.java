package com.dtf.reading_tracker_server.userbook.dto;

import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserBookRequest(
        Long bookId,
        String openLibraryKey,
        @NotNull(message = "status is required")
        ReadingStatus status,
        @Min(value = 1, message = "userRating must be between 1 and 5")
        @Max(value = 5, message = "userRating must be between 1 and 5")
        Integer userRating,
        @Min(value = 0, message = "currentPage must be zero or greater")
        Integer currentPage
) {
        @AssertTrue(message = "bookId or openLibraryKey is required")
        public boolean hasBookIdentifier() {
                return bookId != null || (openLibraryKey != null && !openLibraryKey.isBlank());
        }
}
