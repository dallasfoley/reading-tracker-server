package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;
import com.dtf.reading_tracker_server.shared.config.CorsConfig;
import com.dtf.reading_tracker_server.shared.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void searchRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/books/search").param("query", "dune"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchReturnsOpenLibraryResults() throws Exception {
        when(bookService.search("dune")).thenReturn(List.of(new BookResponse(
                null,
                "/works/OL893415W",
                "Dune",
                "Frank Herbert",
                1965,
                "Science fiction",
                412,
                "https://covers.openlibrary.org/b/id/12345-L.jpg",
                null
        )));

        mockMvc.perform(get("/api/books/search")
                        .param("query", "dune")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].openLibraryKey").value("/works/OL893415W"))
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[0].author").value("Frank Herbert"));
    }

    @Test
    void getReturnsPersistedBook() throws Exception {
        when(bookService.get(10L)).thenReturn(new BookResponse(
                10L,
                "/works/OL893415W",
                "Dune",
                "Frank Herbert",
                1965,
                "Science fiction",
                412,
                "https://covers.openlibrary.org/b/id/12345-L.jpg",
                "A desert planet epic."
        ));

        mockMvc.perform(get("/api/books/10").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.openLibraryKey").value("/works/OL893415W"))
                .andExpect(jsonPath("$.description").value("A desert planet epic."));
    }
}
