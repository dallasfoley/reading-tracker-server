package com.dtf.reading_tracker_server.book.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchDoc(
        String key,
        String title,
        @JsonProperty("author_name")
        List<String> authorNames,
        @JsonProperty("first_publish_year")
        Integer firstPublishYear,
        @JsonProperty("number_of_pages_median")
        Integer numberOfPagesMedian,
        @JsonProperty("cover_i")
        Integer coverId,
        List<String> subject
) {}
