package com.school.library.controller;

import com.school.library.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Flow J: staff can search borrowing history by member or by book. */
@Controller
@RequestMapping("/staff/history")
public class StaffHistoryController {

    private final LoanService loans;

    public StaffHistoryController(LoanService loans) { this.loans = loans; }

    @GetMapping
    public String history(@RequestParam(value = "type", defaultValue = "member") String type,
                          @RequestParam(value = "keyword", required = false) String keyword,
                          Model model) {
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("filtered", keyword != null && !keyword.isBlank());
        model.addAttribute("history", loans.searchHistory(type, keyword));
        return "staff-history";
    }
}
