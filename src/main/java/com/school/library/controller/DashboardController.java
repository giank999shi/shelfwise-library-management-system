package com.school.library.controller;

import com.school.library.model.AppUser;
import com.school.library.model.Loan;
import com.school.library.model.LoanStatus;
import com.school.library.repository.BookRepository;
import com.school.library.repository.LoanRepository;
import com.school.library.repository.MemberRepository;
import com.school.library.repository.UserRepository;
import com.school.library.service.LoanService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class DashboardController {

    private final BookRepository books;
    private final MemberRepository members;
    private final LoanRepository loans;
    private final UserRepository users;
    private final LoanService loanService;

    public DashboardController(BookRepository books, MemberRepository members, LoanRepository loans,
                               UserRepository users, LoanService loanService) {
        this.books = books; this.members = members; this.loans = loans;
        this.users = users; this.loanService = loanService;
    }

    @GetMapping("/")
    public String home() { return "redirect:/dashboard"; }

    /** Set app.demo-accounts=false in application.properties to hide the demo-account buttons. */
    @Value("${app.demo-accounts:true}")
    private boolean demoAccounts;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("demoAccounts", demoAccounts);
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forgot-password"; }

    /** Flow A8: show the dashboard that matches the user's role. */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("catalogBooks", books.findAll(org.springframework.data.domain.Sort.by("title").ascending()));
        boolean admin = hasRole(auth, "ROLE_ADMIN");
        boolean librarian = hasRole(auth, "ROLE_LIBRARIAN");

        if (admin || librarian) {
            model.addAttribute("activeCount", loans.countByStatus(LoanStatus.BORROWED));
            model.addAttribute("overdueCount", loanService.overdue().size());
            model.addAttribute("dueSoonCount", loanService.dueSoon().size());
            if (admin) {
                model.addAttribute("bookCount", books.count());
                model.addAttribute("memberCount", members.count());
                return "dashboard-admin";
            }
            return "dashboard-librarian";
        }

        // Teacher / Student
        var member = users.findByUsername(auth.getName()).map(AppUser::getMember).orElse(null);
        model.addAttribute("hasMember", member != null);
        if (member != null) {
            List<Loan> mine = loanService.historyFor(member.getId());
            model.addAttribute("borrowed", mine.stream().filter(l -> l.getStatus() == LoanStatus.BORROWED).toList());
            model.addAttribute("memberName", member.getName());
            model.addAttribute("limit", member.getMemberType().getMaxBooks());
        }
        return "dashboard-member";
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
