package com.dtf.reading_tracker_server.book;

import com.dtf.reading_tracker_server.book.dto.BookResponse;
import com.dtf.reading_tracker_server.book.dto.OpenLibrarySearchDoc;
import com.dtf.reading_tracker_server.book.dto.OpenLibrarySearchResponse;
import com.dtf.reading_tracker_server.book.dto.OpenLibraryWorkResponse;
import com.dtf.reading_tracker_server.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;
  private final RestClient openLibraryRestClient;

  @Override
  public List<BookResponse> search(String query) {
    OpenLibrarySearchResponse response = openLibraryRestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/search.json")
                    .queryParam("q", query)
                    .queryParam("limit", 10)
                    .queryParam("fields", "key,title,author_name,first_publish_year,number_of_pages_median,cover_i,subject")
                    .build())
            .retrieve()
            .body(OpenLibrarySearchResponse.class);

    return Optional.ofNullable(response)
            .map(OpenLibrarySearchResponse::docs)
            .orElse(Collections.emptyList())
            .stream()
            .map(this::toBookResponse)
            .toList();
  }

  @Override
  public BookResponse get(Long id) {
    return bookRepository.findById(id)
            .map(BookResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
  }

  @Override
  public Book findOrCreateFromOpenLibrary(String openLibraryKey) {
    String normalizedKey = normalizeOpenLibraryKey(openLibraryKey);

    return bookRepository.findByOpenLibraryKey(normalizedKey)
            .orElseGet(() -> bookRepository.save(fetchBookFromOpenLibrary(normalizedKey)));
  }

  private BookResponse toBookResponse(OpenLibrarySearchDoc doc) {
    return new BookResponse(
            null,
            normalizeNullableOpenLibraryKey(doc.key()),
            doc.title(),
            joinOrUnknown(doc.authorNames()),
            doc.firstPublishYear(),
            firstOrNull(doc.subject()),
            doc.numberOfPagesMedian(),
            coverUrl(doc.coverId()),
            null
    );
  }

  private Book fetchBookFromOpenLibrary(String openLibraryKey) {
    OpenLibrarySearchDoc searchDoc = findSearchDocByKey(openLibraryKey)
            .orElseThrow(() -> new ResourceNotFoundException("OpenLibrary book not found"));
    OpenLibraryWorkResponse work = openLibraryRestClient.get()
            .uri(openLibraryKey + ".json")
            .retrieve()
            .body(OpenLibraryWorkResponse.class);

    return Book.builder()
            .openLibraryKey(openLibraryKey)
            .title(Optional.ofNullable(searchDoc.title()).orElse("Untitled"))
            .author(joinOrUnknown(searchDoc.authorNames()))
            .yearPublished(searchDoc.firstPublishYear())
            .genre(firstOrNull(searchDoc.subject()))
            .pageCount(searchDoc.numberOfPagesMedian())
            .coverUrl(coverUrl(searchDoc.coverId()))
            .description(extractDescription(work))
            .build();
  }

  private Optional<OpenLibrarySearchDoc> findSearchDocByKey(String openLibraryKey) {
    OpenLibrarySearchResponse response = openLibraryRestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/search.json")
                    .queryParam("q", openLibraryKey)
                    .queryParam("limit", 1)
                    .queryParam("fields", "key,title,author_name,first_publish_year,number_of_pages_median,cover_i,subject")
                    .build())
            .retrieve()
            .body(OpenLibrarySearchResponse.class);

    return Optional.ofNullable(response)
            .map(OpenLibrarySearchResponse::docs)
            .orElse(Collections.emptyList())
            .stream()
            .filter(doc -> openLibraryKey.equals(normalizeNullableOpenLibraryKey(doc.key())))
            .findFirst();
  }

  private String normalizeOpenLibraryKey(String openLibraryKey) {
    if (openLibraryKey == null || openLibraryKey.isBlank()) {
      throw new ResourceNotFoundException("OpenLibrary key is required");
    }

    String trimmed = openLibraryKey.trim();
    if (trimmed.startsWith("/works/")) {
      return trimmed;
    }
    return "/works/" + trimmed;
  }

  private String normalizeNullableOpenLibraryKey(String openLibraryKey) {
    if (openLibraryKey == null || openLibraryKey.isBlank()) {
      return null;
    }
    return normalizeOpenLibraryKey(openLibraryKey);
  }

  private String joinOrUnknown(List<String> values) {
    if (values == null || values.isEmpty()) {
      return "Unknown";
    }
    return String.join(", ", values);
  }

  private String firstOrNull(List<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.getFirst();
  }

  private String coverUrl(Integer coverId) {
    if (coverId == null) {
      return null;
    }
    return "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
  }

  private String extractDescription(OpenLibraryWorkResponse work) {
    if (work == null || work.description() == null) {
      return null;
    }

    if (work.description() instanceof String description) {
      return description;
    }

    if (work.description() instanceof Map<?, ?> description && description.get("value") instanceof String value) {
      return value;
    }

    return null;
  }

}
