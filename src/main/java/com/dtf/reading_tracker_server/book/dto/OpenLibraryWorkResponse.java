package com.dtf.reading_tracker_server.book.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryWorkResponse(
        String key,
        String title,
        Object description,
        List<String> subjects
) {}
