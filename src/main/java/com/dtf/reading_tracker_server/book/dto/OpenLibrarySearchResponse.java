package com.dtf.reading_tracker_server.book.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchResponse(
        List<OpenLibrarySearchDoc> docs
) {}
