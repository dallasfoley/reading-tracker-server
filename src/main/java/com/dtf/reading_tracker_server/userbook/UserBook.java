package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.book.Book;
import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_books", uniqueConstraints =
        @UniqueConstraint(name = "uk_user_books_user_book", columnNames = {"user_id", "book_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReadingStatus status;

    @Column
    private Integer userRating;

    @Column
    private Integer currentPage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
