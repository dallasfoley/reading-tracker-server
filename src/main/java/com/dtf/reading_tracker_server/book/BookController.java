package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

  @GetMapping("/search?q={query}")
  public ResponseEntity<BookResponse> search(@AuthenticationPrincipal Jwt jwt, @RequestParam String query) {
    return ResponseEntity.ok(bookService.search(query));
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long bookId) {
    return ResponseEntity.ok(bookService.get(bookId));
  }

}
