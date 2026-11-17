package com.school.library.controller;

import com.school.library.model.Member;
import com.school.library.model.MemberType;
import com.school.library.service.LibraryException;
import com.school.library.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Flow E: add, edit, delete, view and search members (students and teachers). */
@Controller
@RequestMapping("/staff/members")
public class MemberAdminController {

    private final MemberService members;

    public MemberAdminController(MemberService members) { this.members = members; }

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("members", members.list(keyword));
        model.addAttribute("keyword", keyword);
        return "members";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Member m = new Member();
        m.setMemberType(MemberType.STUDENT);
        model.addAttribute("member", m);
        model.addAttribute("types", MemberType.values());
        return "member-form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("member", members.get(id));
        model.addAttribute("types", MemberType.values());
        return "member-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value = "id", required = false) Long id,
                       @RequestParam("name") String name,
                       @RequestParam("memberType") MemberType memberType,
                       @RequestParam(value = "active", defaultValue = "false") boolean active,
                       Model model, RedirectAttributes ra) {
        try {
            Member saved = members.save(id, name, memberType, active);
            ra.addFlashAttribute("success", "Saved " + saved.getName() + " (member ID " + saved.getId() + ").");
            return "redirect:/staff/members";
        } catch (LibraryException e) {
            Member m = new Member();
            m.setId(id);
            m.setName(name);
            m.setMemberType(memberType);
            m.setActive(active);
            model.addAttribute("member", m);
            model.addAttribute("types", MemberType.values());
            model.addAttribute("error", e.getMessage());
            return "member-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            members.delete(id);
            ra.addFlashAttribute("success", "Member deleted.");
        } catch (LibraryException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/members";
    }
}
