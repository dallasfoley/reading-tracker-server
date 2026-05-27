package com.dtf.reading_tracker_server.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "books")
@Data
@Builder
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(nullable = false)
  private Integer yearPublished;

  @Column(nullable = false)
  private String genre;

  @Column(nullable = false)
  private Integer pageCount;

  @Column(nullable = false)
  private String coverUrl;

  @Column(length = 2000)
  private String description;
}
