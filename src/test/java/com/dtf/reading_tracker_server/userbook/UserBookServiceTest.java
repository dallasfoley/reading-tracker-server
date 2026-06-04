package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.book.Book;
import com.dtf.reading_tracker_server.book.BookRepository;
import com.dtf.reading_tracker_server.book.BookService;
import com.dtf.reading_tracker_server.exception.ConflictException;
import com.dtf.reading_tracker_server.exception.ResourceNotFoundException;
import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.user.UserRepository;
import com.dtf.reading_tracker_server.user.enums.Role;
import com.dtf.reading_tracker_server.userbook.dto.UserBookRequest;
import com.dtf.reading_tracker_server.userbook.dto.UserBookResponse;
import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBookServiceTest {

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private UserBookServiceImpl userBookService;

    @Test
    void createAddsExistingLocalBookToUserLibrary() {
        User user = user(1L);
        Book book = book(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userBookRepository.existsByUserIdAndBookId(1L, 10L)).thenReturn(false);
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> {
            UserBook userBook = invocation.getArgument(0);
            userBook.setId(100L);
            return userBook;
        });

        UserBookResponse response = userBookService.create(1L, new UserBookRequest(
                10L,
                null,
                ReadingStatus.IN_PROGRESS,
                5,
                42
        ));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(ReadingStatus.IN_PROGRESS);
        assertThat(response.bookId()).isEqualTo(10L);
        assertThat(response.bookTitle()).isEqualTo("Dune");

        ArgumentCaptor<UserBook> savedUserBook = ArgumentCaptor.forClass(UserBook.class);
        verify(userBookRepository).save(savedUserBook.capture());
        assertThat(savedUserBook.getValue().getUser()).isSameAs(user);
        assertThat(savedUserBook.getValue().getBook()).isSameAs(book);
    }

    @Test
    void createAddsOpenLibraryBookToUserLibrary() {
        User user = user(1L);
        Book book = book(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookService.findOrCreateFromOpenLibrary("/works/OL893415W")).thenReturn(book);
        when(userBookRepository.existsByUserIdAndBookId(1L, 10L)).thenReturn(false);
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBookResponse response = userBookService.create(1L, new UserBookRequest(
                null,
                "/works/OL893415W",
                ReadingStatus.NOT_STARTED,
                null,
                0
        ));

        assertThat(response.bookId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(ReadingStatus.NOT_STARTED);
        verify(bookService).findOrCreateFromOpenLibrary("/works/OL893415W");
    }

    @Test
    void createThrowsConflictWhenBookAlreadyExistsInLibrary() {
        User user = user(1L);
        Book book = book(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userBookRepository.existsByUserIdAndBookId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> userBookService.create(1L, new UserBookRequest(
                10L,
                null,
                ReadingStatus.NOT_STARTED,
                null,
                0
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Book already in user's library");
        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void createThrowsWhenRequestHasNoBookIdentifier() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        assertThatThrownBy(() -> userBookService.create(1L, new UserBookRequest(
                null,
                null,
                ReadingStatus.NOT_STARTED,
                null,
                0
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("bookId or openLibraryKey is required");
        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void getAllByUserReturnsUserBooks() {
        when(userBookRepository.findAllByUserId(1L)).thenReturn(List.of(userBook(100L)));

        List<UserBookResponse> responses = userBookService.getAllByUser(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().bookTitle()).isEqualTo("Dune");
    }

    @Test
    void updateStatusUpdatesExistingUserBook() {
        UserBook userBook = userBook(100L);
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(userBook)).thenReturn(userBook);

        UserBookResponse response = userBookService.updateStatus(1L, 10L, "COMPLETED");

        assertThat(response.status()).isEqualTo(ReadingStatus.COMPLETED);
        assertThat(userBook.getStatus()).isEqualTo(ReadingStatus.COMPLETED);
    }

    @Test
    void updateCurrentPageUpdatesExistingUserBook() {
        UserBook userBook = userBook(100L);
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(userBook)).thenReturn(userBook);

        UserBookResponse response = userBookService.updateCurrentPage(1L, 10L, 250);

        assertThat(response.currentPage()).isEqualTo(250);
    }

    @Test
    void updateRatingUpdatesExistingUserBook() {
        UserBook userBook = userBook(100L);
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(userBook)).thenReturn(userBook);

        UserBookResponse response = userBookService.updateRating(1L, 10L, 4);

        assertThat(response.userRating()).isEqualTo(4);
    }

    private static User user(Long id) {
        return User.builder()
                .id(id)
                .authId("auth0|reader")
                .email("reader@example.com")
                .role(Role.USER)
                .build();
    }

    private static Book book(Long id) {
        return Book.builder()
                .id(id)
                .openLibraryKey("/works/OL893415W")
                .title("Dune")
                .author("Frank Herbert")
                .yearPublished(1965)
                .genre("Science fiction")
                .pageCount(412)
                .coverUrl("https://covers.openlibrary.org/b/id/12345-L.jpg")
                .build();
    }

    private static UserBook userBook(Long id) {
        return UserBook.builder()
                .id(id)
                .user(user(1L))
                .book(book(10L))
                .status(ReadingStatus.IN_PROGRESS)
                .userRating(5)
                .currentPage(42)
                .build();
    }
}
