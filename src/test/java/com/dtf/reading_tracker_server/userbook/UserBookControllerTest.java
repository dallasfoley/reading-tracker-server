package com.dtf.reading_tracker_server.userbook;

import com.dtf.reading_tracker_server.shared.config.CorsConfig;
import com.dtf.reading_tracker_server.shared.config.SecurityConfig;
import com.dtf.reading_tracker_server.shared.exception.ConflictException;
import com.dtf.reading_tracker_server.shared.exception.GlobalExceptionHandler;
import com.dtf.reading_tracker_server.user.UserContext;
import com.dtf.reading_tracker_server.userbook.dto.UserBookRequest;
import com.dtf.reading_tracker_server.userbook.dto.UserBookResponse;
import com.dtf.reading_tracker_server.userbook.enums.ReadingStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserBookController.class)
@Import({SecurityConfig.class, CorsConfig.class, GlobalExceptionHandler.class})
class UserBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserBookService userBookService;

    @MockitoBean
    private UserContext userContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void addBookRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/userbooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addBookAcceptsOpenLibraryKeyAndReturnsCreatedUserBook() throws Exception {
        when(userContext.getCurrentUserId(any())).thenReturn(1L);
        when(userBookService.create(any(), any())).thenReturn(response(100L, ReadingStatus.NOT_STARTED));

        mockMvc.perform(post("/api/userbooks")
                        .with(jwt().jwt(token -> token.subject("auth0|reader")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openLibraryKey": "/works/OL893415W",
                                  "status": "NOT_STARTED",
                                  "userRating": null,
                                  "currentPage": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.bookId").value(10L))
                .andExpect(jsonPath("$.bookTitle").value("Dune"));

        ArgumentCaptor<UserBookRequest> request = ArgumentCaptor.forClass(UserBookRequest.class);
        verify(userBookService).create(org.mockito.ArgumentMatchers.eq(1L), request.capture());
        assertThat(request.getValue().openLibraryKey()).isEqualTo("/works/OL893415W");
        assertThat(request.getValue().bookId()).isNull();
    }

    @Test
    void addBookReturnsConflictWhenServiceRejectsDuplicate() throws Exception {
        when(userContext.getCurrentUserId(any())).thenReturn(1L);
        when(userBookService.create(any(), any())).thenThrow(new ConflictException("Book already in user's library"));

        mockMvc.perform(post("/api/userbooks")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 10,
                                  "status": "NOT_STARTED",
                                  "currentPage": 0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book already in user's library"));
    }

    @Test
    void getMyBooksReturnsCurrentUsersLibrary() throws Exception {
        when(userContext.getCurrentUserId(any())).thenReturn(1L);
        when(userBookService.getAllByUser(1L)).thenReturn(List.of(response(100L, ReadingStatus.IN_PROGRESS)));

        mockMvc.perform(get("/api/userbooks").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].bookTitle").value("Dune"));
    }

    @Test
    void updateStatusUsesAuthenticatedUserAndBookId() throws Exception {
        when(userContext.getCurrentUserId(any())).thenReturn(1L);
        when(userBookService.updateStatus(1L, 10L, "COMPLETED")).thenReturn(response(100L, ReadingStatus.COMPLETED));

        mockMvc.perform(patch("/api/userbooks/10/status")
                        .param("status", "COMPLETED")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(userBookService).updateStatus(1L, 10L, "COMPLETED");
    }

    private static UserBookResponse response(Long id, ReadingStatus status) {
        return new UserBookResponse(
                id,
                status,
                5,
                42,
                10L,
                "Dune",
                "Frank Herbert",
                "https://covers.openlibrary.org/b/id/12345-L.jpg"
        );
    }
}
