package com.school.library.service;

public enum ReportType {
    MOST_BORROWED("most-borrowed", "Most Borrowed Books", "Which books are borrowed most often."),
    MONTHLY("monthly", "Monthly Borrowing Report", "Books borrowed and returned in each month."),
    OVERDUE("overdue", "Overdue Books Report", "Books that are past their due date right now."),
    MEMBER("member", "Member Borrowing Report", "Borrowing totals for every student and teacher."),
    RETURNED("returned", "Returned Books Report", "Every returned book, and whether it came back on time.");

    private final String slug;
    private final String title;
    private final String description;

    ReportType(String slug, String title, String description) {
        this.slug = slug; this.title = title; this.description = description;
    }

    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public static ReportType fromSlug(String slug) {
        for (ReportType t : values()) if (t.slug.equals(slug)) return t;
        throw new LibraryException("Unknown report type.");
    }
}
