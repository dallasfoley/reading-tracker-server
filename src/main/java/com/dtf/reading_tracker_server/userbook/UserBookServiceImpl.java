package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.book.Book;
import com.dtf.reading_tracker_server.book.BookRepository;
import com.dtf.reading_tracker_server.book.BookService;
import com.dtf.reading_tracker_server.shared.cache.CacheNames;
import com.dtf.reading_tracker_server.shared.exception.ConflictException;
import com.dtf.reading_tracker_server.shared.exception.InvalidRequestException;
import com.dtf.reading_tracker_server.shared.exception.ResourceNotFoundException;
import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.user.UserRepository;
import com.dtf.reading_tracker_server.userbook.dto.UserBookRequest;
import com.dtf.reading_tracker_server.userbook.dto.UserBookResponse;
import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBookServiceImpl implements UserBookService{

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    @Override
    @Cacheable(cacheNames = CacheNames.USER_BOOKS_BY_USER, key = "#userId")
    public List<UserBookResponse> getAllByUser(Long userId) {
        validatePositive(userId, "user id");

        return userBookRepository.findAllByUserId(userId)
                .stream()
                .map(UserBookResponse::from)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = CacheNames.USER_BOOK_BY_USER_AND_BOOK, key = "#userId + ':' + #bookId")
    public UserBookResponse getByUserAndBook(Long userId, Long bookId) {
        validatePositive(userId, "user id");
        validatePositive(bookId, "book id");

        return userBookRepository.findByUserIdAndBookId(userId, bookId)
                .map(UserBookResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.USER_BOOK_BY_USER_AND_BOOK, key = "#userId + ':' + #result.bookId()"),
            evict = @CacheEvict(cacheNames = CacheNames.USER_BOOKS_BY_USER, key = "#userId")
    )
    public UserBookResponse create(Long userId, UserBookRequest request) {
        validatePositive(userId, "user id");
        validateRequest(request);

        final User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        final Book book = resolveBook(request);

        if (userBookRepository.existsByUserIdAndBookId(userId, book.getId())) {
            throw new ConflictException("Book already in user's library");
        }

        final UserBook userBook = UserBook.builder()
                .user(user)
                .book(book)
                .status(request.status())
                .userRating(request.userRating())
                .currentPage(request.currentPage())
                .build();

        return UserBookResponse.from(userBookRepository.save(userBook));
    }

    private Book resolveBook(UserBookRequest request) {
        if (request.bookId() != null) {
            return bookRepository.findById(request.bookId()).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        }

        if (request.openLibraryKey() != null && !request.openLibraryKey().isBlank()) {
            return bookService.findOrCreateFromOpenLibrary(request.openLibraryKey());
        }

        throw new ResourceNotFoundException("bookId or openLibraryKey is required");
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.USER_BOOKS_BY_USER, key = "#userId"),
            @CacheEvict(cacheNames = CacheNames.USER_BOOK_BY_USER_AND_BOOK, key = "#userId + ':' + #bookId")
    })
    public void delete(Long userId, Long bookId) {
        validatePositive(userId, "user id");
        validatePositive(bookId, "book id");

        final UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId).orElseThrow(() -> new ResourceNotFoundException("UserBook not found"));
        userBookRepository.delete(userBook);
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.USER_BOOK_BY_USER_AND_BOOK, key = "#userId + ':' + #bookId"),
            evict = @CacheEvict(cacheNames = CacheNames.USER_BOOKS_BY_USER, key = "#userId")
    )
    public UserBookResponse updateStatus(Long userId, Long bookId, String status) {
        validatePositive(userId, "user id");
        validatePositive(bookId, "book id");

        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
        userBook.setStatus(parseStatus(status));
        return UserBookResponse.from(userBookRepository.save(userBook));
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.USER_BOOK_BY_USER_AND_BOOK, key = "#userId + ':' + #bookId"),
            evict = @CacheEvict(cacheNames = CacheNames.USER_BOOKS_BY_USER, key = "#userId")
    )
    public UserBookResponse updateCurrentPage(Long userId, Long bookId, Integer currentPage) {
        validatePositive(userId, "user id");
        validatePositive(bookId, "book id");
        if (currentPage == null || currentPage < 0) {
            throw new InvalidRequestException("currentPage must be zero or greater");
        }

        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
        userBook.setCurrentPage(currentPage);
        return UserBookResponse.from(userBookRepository.save(userBook));
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.USER_BOOK_BY_USER_AND_BOOK, key = "#userId + ':' + #bookId"),
            evict = @CacheEvict(cacheNames = CacheNames.USER_BOOKS_BY_USER, key = "#userId")
    )
    public UserBookResponse updateRating(Long userId, Long bookId, Integer rating) {
        validatePositive(userId, "user id");
        validatePositive(bookId, "book id");
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidRequestException("rating must be between 1 and 5");
        }

        final UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
        userBook.setUserRating(rating);
        return UserBookResponse.from(userBookRepository.save(userBook));
    }

    private void validateRequest(UserBookRequest request) {
        if (request == null) {
            throw new InvalidRequestException("request body is required");
        }

        if (request.bookId() == null && (request.openLibraryKey() == null || request.openLibraryKey().isBlank())) {
            throw new InvalidRequestException("bookId or openLibraryKey is required");
        }

        if (request.bookId() != null && request.bookId() <= 0) {
            throw new InvalidRequestException("bookId must be positive");
        }

        if (request.status() == null) {
            throw new InvalidRequestException("status is required");
        }

        if (request.currentPage() != null && request.currentPage() < 0) {
            throw new InvalidRequestException("currentPage must be zero or greater");
        }

        if (request.userRating() != null && (request.userRating() < 1 || request.userRating() > 5)) {
            throw new InvalidRequestException("userRating must be between 1 and 5");
        }
    }

    private ReadingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new InvalidRequestException("status is required");
        }

        try {
            return ReadingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid reading status: " + status);
        }
    }

    private void validatePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new InvalidRequestException(fieldName + " must be positive");
        }
    }
}
