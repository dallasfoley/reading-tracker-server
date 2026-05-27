package com.dtf.reading_tracker_server.book;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

}
