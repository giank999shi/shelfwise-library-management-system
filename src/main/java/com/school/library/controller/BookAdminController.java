package com.school.library.controller;

import com.school.library.model.Book;
import com.school.library.service.BookImageStorageService;
import com.school.library.service.BookService;
import com.school.library.service.LibraryException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/** Flow D: add, edit, delete and view books. */
@Controller
@RequestMapping("/staff/books")
public class BookAdminController {

    private final BookService books;
    private final BookImageStorageService images;

    public BookAdminController(BookService books, BookImageStorageService images) {
        this.books = books;
        this.images = images;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("books", books.list());
        return "books";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("book", new Book());
        return "book-form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("book", books.get(id));
        return "book-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value = "id", required = false) Long id,
                       @RequestParam("title") String title,
                       @RequestParam("author") String author,
                       @RequestParam("totalCopies") int totalCopies,
                       @RequestParam(value = "image", required = false) MultipartFile image,
                       Model model, RedirectAttributes ra) {
        Book existing = id == null ? null : books.get(id);
        String oldImagePath = existing == null ? null : existing.getImagePath();
        String newImagePath = null;
        try {
            if (image != null && !image.isEmpty()) newImagePath = images.store(image);
            Book saved = id == null ? books.add(title, author, totalCopies, newImagePath)
                                    : books.update(id, title, author, totalCopies, newImagePath == null ? oldImagePath : newImagePath);
            if (newImagePath != null && oldImagePath != null) images.delete(oldImagePath);
            ra.addFlashAttribute("success", "Saved \"" + saved.getTitle() + "\".");
            return "redirect:/staff/books";
        } catch (LibraryException | IllegalArgumentException | IOException e) {
            if (newImagePath != null) images.delete(newImagePath);
            Book b = existing == null ? new Book() : existing;
            b.setId(id);
            b.setTitle(title);
            b.setAuthor(author);
            b.setTotalCopies(totalCopies);
            model.addAttribute("book", b);
            model.addAttribute("error", e.getMessage());
            return "book-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            Book book = books.get(id);
            books.delete(id);
            images.delete(book.getImagePath());
            ra.addFlashAttribute("success", "Book deleted.");
        } catch (LibraryException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/books";
    }
}
