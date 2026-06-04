package com.dtf.reading_tracker_server.integration;

import com.dtf.reading_tracker_server.book.BookRepository;
import com.dtf.reading_tracker_server.user.User;
import com.dtf.reading_tracker_server.user.UserRepository;
import com.dtf.reading_tracker_server.user.enums.Role;
import com.dtf.reading_tracker_server.userbook.UserBookRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class BookUserBookIntegrationTest {

    private static final OpenLibraryStub OPEN_LIBRARY = OpenLibraryStub.start();

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "https://auth.example.test/");
        registry.add("okta.oauth2.issuer", () -> "https://auth.example.test/");
        registry.add("open-library.base-url", OPEN_LIBRARY::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserBookRepository userBookRepository;

    @BeforeEach
    void setUp() {
        userBookRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(User.builder()
                .authId("auth0|reader")
                .email("reader@example.com")
                .role(Role.USER)
                .build());
    }

    @AfterAll
    static void stopOpenLibrary() {
        OPEN_LIBRARY.stop();
    }

    @Test
    void searchDoesNotPersistBookUntilUserAddsItToLibrary() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "dune")
                        .with(jwt().jwt(token -> token.subject("auth0|reader"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].openLibraryKey").value("/works/OL893415W"))
                .andExpect(jsonPath("$[0].title").value("Dune"));

        assertThat(bookRepository.count()).isZero();

        mockMvc.perform(post("/api/userbooks")
                        .with(jwt().jwt(token -> token.subject("auth0|reader")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openLibraryKey": "/works/OL893415W",
                                  "status": "NOT_STARTED",
                                  "currentPage": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookTitle").value("Dune"))
                .andExpect(jsonPath("$.bookAuthor").value("Frank Herbert"))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));

        assertThat(bookRepository.count()).isEqualTo(1);
        assertThat(userBookRepository.count()).isEqualTo(1);
        assertThat(bookRepository.findByOpenLibraryKey("/works/OL893415W"))
                .isPresent()
                .get()
                .satisfies(book -> {
                    assertThat(book.getTitle()).isEqualTo("Dune");
                    assertThat(book.getDescription()).isEqualTo("A desert planet epic.");
                });

        mockMvc.perform(post("/api/userbooks")
                        .with(jwt().jwt(token -> token.subject("auth0|reader")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openLibraryKey": "/works/OL893415W",
                                  "status": "NOT_STARTED",
                                  "currentPage": 0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book already in user's library"));

        assertThat(bookRepository.count()).isEqualTo(1);
        assertThat(userBookRepository.count()).isEqualTo(1);
    }

    private static final class OpenLibraryStub {
        private final HttpServer server;

        private OpenLibraryStub(HttpServer server) {
            this.server = server;
        }

        static OpenLibraryStub start() {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
                server.createContext("/", OpenLibraryStub::handle);
                server.start();
                return new OpenLibraryStub(server);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to start OpenLibrary stub", ex);
            }
        }

        String baseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        void stop() {
            server.stop(0);
        }

        private static void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getRawQuery();

            if ("/search.json".equals(path) && query != null && query.contains("limit=10")) {
                respond(exchange, 200, searchBody());
                return;
            }

            if ("/search.json".equals(path) && query != null && query.contains("limit=1")) {
                respond(exchange, 200, searchBody());
                return;
            }

            if ("/works/OL893415W.json".equals(path)) {
                respond(exchange, 200, """
                        {
                          "key": "/works/OL893415W",
                          "title": "Dune",
                          "description": {"type": "/type/text", "value": "A desert planet epic."}
                        }
                        """);
                return;
            }

            respond(exchange, 404, "{\"error\":\"not found\"}");
        }

        private static String searchBody() {
            return """
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
                    """;
        }

        private static void respond(HttpExchange exchange, int status, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }
}
