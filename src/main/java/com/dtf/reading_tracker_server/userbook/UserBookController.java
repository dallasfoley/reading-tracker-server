package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.user.UserContext;
import com.dtf.reading_tracker_server.userbook.dto.UserBookRequest;
import com.dtf.reading_tracker_server.userbook.dto.UserBookResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userbooks")
@RequiredArgsConstructor
@Validated
public class UserBookController {

    private final UserBookService userBookService;
    private final UserContext userContext;

    @GetMapping
    public ResponseEntity<List<UserBookResponse>> getMyBooks(@AuthenticationPrincipal Jwt jwt) {
        final Long userId = userContext.getCurrentUserId(jwt);
        return ResponseEntity.ok(userBookService.getAllByUser(userId));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<UserBookResponse> getByBookId(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable @Positive Long bookId) {
        final Long userId = userContext.getCurrentUserId(jwt);
        return ResponseEntity.ok(userBookService.getByUserAndBook(userId, bookId));
    }

    @PostMapping
    public ResponseEntity<UserBookResponse> addBook(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody UserBookRequest request) {
        final Long userId = userContext.getCurrentUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userBookService.create(userId, request));
    }

    // PATCH /api/userbooks/{bookId}/status?status=READING
    @PatchMapping("/{bookId}/status")
    public ResponseEntity<UserBookResponse> updateStatus(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable @Positive Long bookId,
                                                         @RequestParam String status) {
        final Long userId = userContext.getCurrentUserId(jwt);
        return ResponseEntity.ok(userBookService.updateStatus(userId, bookId, status));
    }

    // PATCH /api/userbooks/{bookId}/current-page?currentPage=120
    @PatchMapping("/{bookId}/current-page")
    public ResponseEntity<UserBookResponse> updateCurrentPage(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable @Positive Long bookId,
                                                              @RequestParam @Min(0) int currentPage) {
        final Long userId = userContext.getCurrentUserId(jwt);
        return ResponseEntity.ok(userBookService.updateCurrentPage(userId, bookId, currentPage));
    }

    // PATCH /api/userbooks/{bookId}/rating?rating=4
    @PatchMapping("/{bookId}/rating")
    public ResponseEntity<UserBookResponse> updateRating(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable @Positive Long bookId,
                                                         @RequestParam @Min(1) @Max(5) int rating) {
        final Long userId = userContext.getCurrentUserId(jwt);
        return ResponseEntity.ok(userBookService.updateRating(userId, bookId, rating));
    }

    // DELETE /api/userbooks/{bookId}
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable @Positive Long bookId) {
        final Long userId = userContext.getCurrentUserId(jwt);
        userBookService.delete(userId, bookId);
        return ResponseEntity.noContent().build();
    }
}
