package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated
public class BookController {

  private final BookService bookService;

  @GetMapping("/search")
  public ResponseEntity<List<BookResponse>> search(@RequestParam @NotBlank String query) {
    return ResponseEntity.ok(bookService.search(query));
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookResponse> get(@PathVariable @Positive Long bookId) {
    return ResponseEntity.ok(bookService.get(bookId));
  }

}
