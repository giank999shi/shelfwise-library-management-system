package com.school.library.config;

import com.school.library.service.LibraryException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Safety net: a friendly message instead of an error page (e.g. opening a deleted record). */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Lets the sidebar highlight the page you are on. */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ExceptionHandler(LibraryException.class)
    public String handle(LibraryException e, RedirectAttributes ra) {
        ra.addFlashAttribute("error", e.getMessage());
        return "redirect:/dashboard";
    }
}
