package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

  @GetMapping("/search")
  public ResponseEntity<List<BookResponse>> search(@RequestParam String query) {
    return ResponseEntity.ok(bookService.search(query));
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookResponse> get(@PathVariable Long bookId) {
    return ResponseEntity.ok(bookService.get(bookId));
  }

}
