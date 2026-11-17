package com.school.library.controller;

import com.school.library.service.LibraryException;
import com.school.library.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/loans")
public class LoanController {

    private final LoanService loans;

    public LoanController(LoanService loans) { this.loans = loans; }

    @GetMapping("/borrow")
    public String borrowForm(Model model) {
        model.addAttribute("activeLoans", loans.activeLoans());
        return "borrow";
    }

    @PostMapping("/borrow")
    public String borrow(@RequestParam Long memberId, @RequestParam Long bookId, RedirectAttributes ra) {
        try {
            var loan = loans.borrow(memberId, bookId);
            ra.addFlashAttribute("success", "Borrowed. Due on " + loan.getDueDate() + ".");
        } catch (LibraryException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/loans/borrow";
    }

    @PostMapping("/return")
    public String returnBook(@RequestParam Long loanId, RedirectAttributes ra) {
        try {
            loans.returnBook(loanId);
            ra.addFlashAttribute("success", "Book returned.");
        } catch (LibraryException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/loans/borrow";
    }

    @GetMapping("/overdue")
    public String overdue(Model model) {
        model.addAttribute("overdue", loans.overdue());
        model.addAttribute("dueSoon", loans.dueSoon());
        return "overdue";
    }
}
