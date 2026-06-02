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
@Table(name = "user_books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
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
