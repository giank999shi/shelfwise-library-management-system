package com.school.library.service;

import java.util.List;

/** A generated report: a title, column headers and rows of text. */
public class Report {
    private final String title;
    private final String description;
    private final List<String> headers;
    private final List<List<String>> rows;

    public Report(String title, String description, List<String> headers, List<List<String>> rows) {
        this.title = title; this.description = description; this.headers = headers; this.rows = rows;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getHeaders() { return headers; }
    public List<List<String>> getRows() { return rows; }
}
