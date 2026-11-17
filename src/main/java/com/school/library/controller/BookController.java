package com.school.library.controller;

import com.school.library.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookController {

    private final BookService books;

    public BookController(BookService books) { this.books = books; }

    /** Flow I: search by Book ID, title or author. Open to every logged-in role. */
    @GetMapping("/books/search")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "title") String category,
                         Model model) {
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        if (keyword != null && !keyword.isBlank()) {
            try {
                model.addAttribute("results", books.search(category, keyword.trim()));
                model.addAttribute("searched", true);
            } catch (NumberFormatException e) {
                model.addAttribute("error", "Book ID must be a number.");
            }
        }
        return "search";
    }
}
