package com.school.library.controller;

import com.school.library.model.AppUser;
import com.school.library.model.Role;
import com.school.library.service.LibraryException;
import com.school.library.service.MemberService;
import com.school.library.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin: manage user accounts and librarians. */
@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    private final UserService users;
    private final MemberService members;

    public UserAdminController(UserService users, MemberService members) {
        this.users = users; this.members = members;
    }

    @GetMapping
    public String list(@RequestParam(value = "role", required = false) Role role, Model model) {
        model.addAttribute("users", users.list(role));
        model.addAttribute("roleFilter", role);
        model.addAttribute("roles", Role.values());
        return "users";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        AppUser u = new AppUser();
        u.setRole(Role.LIBRARIAN);
        fillForm(model, u, null);
        return "user-form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        AppUser u = users.get(id);
        fillForm(model, u, u.getMember() == null ? null : u.getMember().getId());
        return "user-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value = "id", required = false) Long id,
                       @RequestParam("username") String username,
                       @RequestParam(value = "password", required = false) String password,
                       @RequestParam("role") Role role,
                       @RequestParam(value = "memberId", required = false) Long memberId,
                       @RequestParam(value = "active", defaultValue = "false") boolean active,
                       Authentication auth, Model model, RedirectAttributes ra) {
        try {
            AppUser saved = id == null
                    ? users.create(username, password, role, memberId, active)
                    : users.update(id, password, role, memberId, active, auth.getName());
            ra.addFlashAttribute("success", "Saved account '" + saved.getUsername() + "'.");
            return "redirect:/admin/users";
        } catch (LibraryException e) {
            AppUser u = new AppUser();
            u.setId(id);
            u.setUsername(username);
            u.setRole(role);
            u.setActive(active);
            fillForm(model, u, memberId);
            model.addAttribute("error", e.getMessage());
            return "user-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, Authentication auth, RedirectAttributes ra) {
        try {
            users.delete(id, auth.getName());
            ra.addFlashAttribute("success", "Account deleted.");
        } catch (LibraryException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void fillForm(Model model, AppUser u, Long selectedMemberId) {
        model.addAttribute("u", u);
        model.addAttribute("roles", Role.values());
        model.addAttribute("members", members.all());
        model.addAttribute("selectedMemberId", selectedMemberId);
    }
}
