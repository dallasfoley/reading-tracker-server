package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;
import com.dtf.reading_tracker_server.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookServiceTest {

    private BookRepository bookRepository;
    private BookServiceImpl bookService;
    private MockRestServiceServer openLibrary;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openlibrary.test");
        openLibrary = MockRestServiceServer.bindTo(builder).build();
        bookService = new BookServiceImpl(bookRepository, builder.build());
    }

    @Test
    void searchReturnsOpenLibraryResultsWithoutSavingBooks() {
        openLibrary.expect(once(), requestTo(allOf(
                        containsString("https://openlibrary.test/search.json"),
                        containsString("q=dune"),
                        containsString("limit=10"),
                        containsString("fields=")
                )))
                .andRespond(withSuccess("""
                        {
                          "docs": [
                            {
                              "key": "OL893415W",
                              "title": "Dune",
                              "author_name": ["Frank Herbert"],
                              "first_publish_year": 1965,
                              "number_of_pages_median": 412,
                              "cover_i": 12345,
                              "subject": ["Science fiction"]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<BookResponse> results = bookService.search("dune");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst())
                .extracting(
                        BookResponse::id,
                        BookResponse::openLibraryKey,
                        BookResponse::title,
                        BookResponse::author,
                        BookResponse::yearPublished,
                        BookResponse::genre,
                        BookResponse::pageCount,
                        BookResponse::coverUrl,
                        BookResponse::description
                )
                .containsExactly(
                        null,
                        "/works/OL893415W",
                        "Dune",
                        "Frank Herbert",
                        1965,
                        "Science fiction",
                        412,
                        "https://covers.openlibrary.org/b/id/12345-L.jpg",
                        null
                );
        verify(bookRepository, never()).save(any(Book.class));
        openLibrary.verify();
    }

    @Test
    void getReturnsPersistedBook() {
        Book book = Book.builder()
                .id(12L)
                .openLibraryKey("/works/OL1W")
                .title("Persisted Book")
                .author("Known Author")
                .yearPublished(2020)
                .genre("Fantasy")
                .pageCount(321)
                .coverUrl("https://example.test/cover.jpg")
                .description("Stored description")
                .build();
        when(bookRepository.findById(12L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.get(12L);

        assertThat(response.id()).isEqualTo(12L);
        assertThat(response.openLibraryKey()).isEqualTo("/works/OL1W");
        assertThat(response.title()).isEqualTo("Persisted Book");
    }

    @Test
    void getThrowsWhenBookDoesNotExist() {
        when(bookRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.get(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    void findOrCreateFromOpenLibraryReusesExistingBook() {
        Book existing = Book.builder()
                .id(7L)
                .openLibraryKey("/works/OL7W")
                .title("Existing")
                .author("Author")
                .build();
        when(bookRepository.findByOpenLibraryKey("/works/OL7W")).thenReturn(Optional.of(existing));

        Book result = bookService.findOrCreateFromOpenLibrary("OL7W");

        assertThat(result).isSameAs(existing);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void findOrCreateFromOpenLibraryFetchesAndSavesMissingBook() {
        when(bookRepository.findByOpenLibraryKey("/works/OL893415W")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(99L);
            return book;
        });

        openLibrary.expect(once(), requestTo(allOf(
                        containsString("https://openlibrary.test/search.json"),
                        containsString("q=key:"),
                        containsString("OL893415W"),
                        containsString("limit=1")
                )))
                .andRespond(withSuccess("""
                        {
                          "docs": [
                            {
                              "key": "/works/OL893415W",
                              "title": "Dune",
                              "author_name": ["Frank Herbert"],
                              "first_publish_year": 1965,
                              "number_of_pages_median": 412,
                              "cover_i": 12345,
                              "subject": ["Science fiction"]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));
        openLibrary.expect(once(), requestTo("https://openlibrary.test/works/OL893415W.json"))
                .andRespond(withSuccess("""
                        {
                          "key": "/works/OL893415W",
                          "title": "Dune",
                          "description": {"type": "/type/text", "value": "A desert planet epic."}
                        }
                        """, MediaType.APPLICATION_JSON));

        Book result = bookService.findOrCreateFromOpenLibrary("OL893415W");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getOpenLibraryKey()).isEqualTo("/works/OL893415W");
        assertThat(result.getDescription()).isEqualTo("A desert planet epic.");

        ArgumentCaptor<Book> savedBook = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(savedBook.capture());
        assertThat(savedBook.getValue().getTitle()).isEqualTo("Dune");
        assertThat(savedBook.getValue().getAuthor()).isEqualTo("Frank Herbert");
        assertThat(savedBook.getValue().getGenre()).isEqualTo("Science fiction");
        openLibrary.verify();
    }

    @Test
    void findOrCreateFromOpenLibraryThrowsWhenSearchCannotFindWork() {
        when(bookRepository.findByOpenLibraryKey("/works/OL404W")).thenReturn(Optional.empty());
        openLibrary.expect(once(), requestTo(containsString("https://openlibrary.test/search.json")))
                .andRespond(withSuccess("{\"docs\": []}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> bookService.findOrCreateFromOpenLibrary("/works/OL404W"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("OpenLibrary book not found");
        verify(bookRepository, never()).save(any(Book.class));
        openLibrary.verify();
    }
}
