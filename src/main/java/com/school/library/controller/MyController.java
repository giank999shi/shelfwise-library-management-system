package com.school.library.controller;

import com.school.library.model.AppUser;
import com.school.library.repository.UserRepository;
import com.school.library.service.LoanService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyController {

    private final UserRepository users;
    private final LoanService loans;

    public MyController(UserRepository users, LoanService loans) {
        this.users = users; this.loans = loans;
    }

    /** Teacher/Student: personal borrowing history with due dates and status. */
    @GetMapping("/my/history")
    public String history(Authentication auth, Model model) {
        var member = users.findByUsername(auth.getName()).map(AppUser::getMember).orElse(null);
        model.addAttribute("hasMember", member != null);
        if (member != null) model.addAttribute("history", loans.historyFor(member.getId()));
        return "history";
    }
}
