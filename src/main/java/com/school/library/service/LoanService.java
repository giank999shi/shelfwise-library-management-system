package com.school.library.service;

import com.school.library.model.*;
import com.school.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loans;
    private final BookRepository books;
    private final MemberRepository members;
    private final ActivityLogService logs;

    public LoanService(LoanRepository loans, BookRepository books, MemberRepository members, ActivityLogService logs) {
        this.loans = loans; this.books = books; this.members = members; this.logs = logs;
    }

    /** Flow F: borrow a book. */
    @Transactional
    public Loan borrow(Long memberId, Long bookId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new LibraryException("Member not found."));
        if (!member.isActive()) throw new LibraryException("Member is not active.");

        MemberType type = member.getMemberType();
        long current = loans.countByMemberIdAndStatus(memberId, LoanStatus.BORROWED);
        if (current >= type.getMaxBooks())
            throw new LibraryException("Borrowing limit reached (" + type.getMaxBooks() + " books for a "
                    + type.getLabel().toLowerCase() + ").");

        Book book = books.findById(bookId)
                .orElseThrow(() -> new LibraryException("Book not found."));
        if (book.getAvailableCopies() <= 0) throw new LibraryException("Book not available.");

        LocalDate today = LocalDate.now();
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setBorrowDate(today);
        loan.setDueDate(today.plusDays(type.getLoanDays()));
        loan.setStatus(LoanStatus.BORROWED);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        books.save(book);
        loan = loans.save(loan);
        logs.log("Borrowed '" + book.getTitle() + "' (book #" + book.getId() + ") for " + member.getName()
                + " (member #" + member.getId() + "), due " + loan.getDueDate());
        return loan;
    }

    /** Flow G: return a book. */
    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loans.findById(loanId)
                .orElseThrow(() -> new LibraryException("Loan not found."));
        if (loan.getStatus() != LoanStatus.BORROWED)
            throw new LibraryException("Invalid or already returned.");

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        books.save(book);
        loan = loans.save(loan);
        logs.log("Returned '" + book.getTitle() + "' (book #" + book.getId() + ") from "
                + loan.getMember().getName() + " (loan #" + loan.getId() + ")");
        return loan;
    }

    /** Flow H: overdue and due-soon lists. */
    public List<Loan> overdue() {
        return loans.findByStatusAndDueDateBefore(LoanStatus.BORROWED, LocalDate.now());
    }

    public List<Loan> dueSoon() {
        LocalDate today = LocalDate.now();
        return loans.findByStatusAndDueDateBetween(LoanStatus.BORROWED, today, today.plusDays(2));
    }

    public List<Loan> activeLoans() {
        return loans.findByStatusOrderByDueDateAsc(LoanStatus.BORROWED);
    }

    /** Flow J: one member's history. */
    public List<Loan> historyFor(Long memberId) {
        return loans.findByMemberIdOrderByBorrowDateDesc(memberId);
    }

    /** Flow J (staff): search history by member name or book title; empty search = latest 100 loans. */
    public List<Loan> searchHistory(String type, String keyword) {
        if (keyword == null || keyword.isBlank()) return loans.findTop100ByOrderByBorrowDateDescIdDesc();
        String k = keyword.trim();
        return "book".equals(type)
                ? loans.findByBookTitleContainingIgnoreCaseOrderByBorrowDateDesc(k)
                : loans.findByMemberNameContainingIgnoreCaseOrderByBorrowDateDesc(k);
    }
}
