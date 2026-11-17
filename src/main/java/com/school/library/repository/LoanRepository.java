package com.school.library.repository;

import com.school.library.model.Loan;
import com.school.library.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    long countByMemberIdAndStatus(Long memberId, LoanStatus status);
    long countByStatus(LoanStatus status);
    boolean existsByBookIdAndStatus(Long bookId, LoanStatus status);
    boolean existsByBookId(Long bookId);
    boolean existsByMemberId(Long memberId);
    List<Loan> findByStatusOrderByDueDateAsc(LoanStatus status);
    List<Loan> findByStatusOrderByReturnDateDesc(LoanStatus status);
    List<Loan> findByMemberIdOrderByBorrowDateDesc(Long memberId);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDate date);
    List<Loan> findByStatusAndDueDateBetween(LoanStatus status, LocalDate from, LocalDate to);
    List<Loan> findTop100ByOrderByBorrowDateDescIdDesc();
    List<Loan> findByMemberNameContainingIgnoreCaseOrderByBorrowDateDesc(String name);
    List<Loan> findByBookTitleContainingIgnoreCaseOrderByBorrowDateDesc(String title);
}
