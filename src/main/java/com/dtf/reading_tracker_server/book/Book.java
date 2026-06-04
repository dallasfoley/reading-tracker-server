package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.userbook.UserBook;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserBook> userBooks = new ArrayList<>();

  @Column(unique = true)
  private String openLibraryKey;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column
  private Integer yearPublished;

  @Column
  private String genre;

  @Column
  private Integer pageCount;

  @Column
  private String coverUrl;

  @Column(length = 2000)
  private String description;
}
