package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.book.Book;
import com.dtf.reading_tracker_server.book.BookRepository;
import com.dtf.reading_tracker_server.book.BookService;
import com.dtf.reading_tracker_server.exception.ConflictException;
import com.dtf.reading_tracker_server.exception.ResourceNotFoundException;
import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.user.UserRepository;
import com.dtf.reading_tracker_server.userbook.dto.UserBookRequest;
import com.dtf.reading_tracker_server.userbook.dto.UserBookResponse;
import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserBookServiceImpl implements UserBookService{

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    @Override
    public List<UserBookResponse> getAllByUser(Long userId) {
        return userBookRepository.findAllByUserId(userId)
                .stream()
                .map(UserBookResponse::from)
                .toList();
    }

    @Override
    public UserBookResponse getByUserAndBook(Long userId, Long bookId) {
        return userBookRepository.findByUserIdAndBookId(userId, bookId)
                .map(UserBookResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
    }

    @Override
    public UserBookResponse create(Long userId, UserBookRequest request) {

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
    public void delete(Long userId, Long bookId) {
        final UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId).orElseThrow(() -> new ResourceNotFoundException("UserBook not found"));
        userBookRepository.delete(userBook);
    }

    @Override
    public UserBookResponse updateStatus(Long userId, Long bookId, String status) {
        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
        userBook.setStatus(ReadingStatus.valueOf(status));
        return UserBookResponse.from(userBookRepository.save(userBook));
    }

    @Override
    public UserBookResponse updateCurrentPage(Long userId, Long bookId, Integer currentPage) {
        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
        userBook.setCurrentPage(currentPage);
        return UserBookResponse.from(userBookRepository.save(userBook));
    }

    @Override
    public UserBookResponse updateRating(Long userId, Long bookId, Integer rating) {
        final UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in user's library"));
        userBook.setUserRating(rating);
        return UserBookResponse.from(userBookRepository.save(userBook));
    }
}
