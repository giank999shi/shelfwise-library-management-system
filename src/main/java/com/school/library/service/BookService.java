package com.school.library.service;

import com.school.library.model.Book;
import com.school.library.repository.BookRepository;
import com.school.library.repository.LoanRepository;
import com.school.library.model.LoanStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BookService {

    private final BookRepository books;
    private final LoanRepository loans;
    private final ActivityLogService logs;

    public BookService(BookRepository books, LoanRepository loans, ActivityLogService logs) {
        this.books = books; this.loans = loans; this.logs = logs;
    }

    public List<Book> list() { return books.findAll(Sort.by("title").ascending()); }

    public Book get(Long id) {
        return books.findById(id).orElseThrow(() -> new LibraryException("Book not found."));
    }

    /** Flow D4: validate, save, log. */
    @Transactional
    public Book add(String title, String author, int totalCopies, String imagePath) {
        Book b = new Book();
        b.setTitle(clean(title, "Title", 200));
        b.setAuthor(clean(author, "Author", 150));
        checkCopies(totalCopies);
        b.setTotalCopies(totalCopies);
        b.setAvailableCopies(totalCopies);
        b.setImagePath(imagePath);
        b = books.save(b);
        logs.log("Added book #" + b.getId() + " '" + b.getTitle() + "' (" + totalCopies + " copies)");
        return b;
    }

    /** Flow D5: edit. Available copies are adjusted so borrowed copies stay correct. */
    @Transactional
    public Book update(Long id, String title, String author, int totalCopies, String imagePath) {
        Book b = get(id);
        String t = clean(title, "Title", 200);
        String a = clean(author, "Author", 150);
        checkCopies(totalCopies);
        int borrowed = b.getTotalCopies() - b.getAvailableCopies();
        if (totalCopies < borrowed)
            throw new LibraryException("Total copies can't be less than the " + borrowed + " currently borrowed.");
        b.setTitle(t);
        b.setAuthor(a);
        b.setTotalCopies(totalCopies);
        b.setAvailableCopies(totalCopies - borrowed);
        b.setImagePath(imagePath);
        b = books.save(b);
        logs.log("Edited book #" + b.getId() + " '" + b.getTitle() + "'");
        return b;
    }

    /** Flow D6: block deletion while the book has active loans. */
    @Transactional
    public void delete(Long id) {
        Book b = get(id);
        if (loans.existsByBookIdAndStatus(id, LoanStatus.BORROWED))
            throw new LibraryException("Cannot delete: this book has active loans.");
        if (loans.existsByBookId(id))
            throw new LibraryException("Cannot delete: this book appears in the borrowing history, which must be kept.");
        books.delete(b);
        logs.log("Deleted book #" + id + " '" + b.getTitle() + "'");
    }

    /** Flow I: search by ID, title or author. */
    public List<Book> search(String category, String keyword) {
        return switch (category) {
            case "id" -> books.findById(Long.parseLong(keyword)).map(b -> List.of(b)).orElse(List.of());
            case "author" -> books.findByAuthorContainingIgnoreCase(keyword);
            default -> books.findByTitleContainingIgnoreCase(keyword);
        };
    }

    private String clean(String value, String label, int max) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) throw new LibraryException(label + " is required.");
        if (v.length() > max) throw new LibraryException(label + " is too long (max " + max + " characters).");
        return v;
    }

    private void checkCopies(int copies) {
        if (copies < 1) throw new LibraryException("Copies must be at least 1.");
        if (copies > 1000) throw new LibraryException("Copies can't be more than 1000.");
    }
}
