package com.school.library.service;

import com.school.library.model.AppUser;
import com.school.library.model.Member;
import com.school.library.model.MemberType;
import com.school.library.model.Role;
import com.school.library.repository.MemberRepository;
import com.school.library.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.regex.Pattern;

/** Admin-only: user accounts and librarians. */
@Service
public class UserService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");

    private final UserRepository users;
    private final MemberRepository members;
    private final PasswordEncoder encoder;
    private final ActivityLogService logs;

    public UserService(UserRepository users, MemberRepository members, PasswordEncoder encoder, ActivityLogService logs) {
        this.users = users; this.members = members; this.encoder = encoder; this.logs = logs;
    }

    public List<AppUser> list(Role role) {
        return role == null ? users.findAll(Sort.by("id")) : users.findByRoleOrderByUsernameAsc(role);
    }

    public AppUser get(Long id) {
        return users.findById(id).orElseThrow(() -> new LibraryException("Account not found."));
    }

    @Transactional
    public AppUser create(String username, String password, Role role, Long memberId, boolean active) {
        String name = username == null ? "" : username.trim();
        if (!USERNAME.matcher(name).matches())
            throw new LibraryException("Username must be 3-50 characters: letters, numbers, dot, dash or underscore.");
        if (users.existsByUsername(name)) throw new LibraryException("That username is already taken.");
        checkPassword(password);
        if (role == null) throw new LibraryException("Please choose a role.");

        AppUser u = new AppUser();
        u.setUsername(name);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        u.setMember(resolveMember(role, memberId, null));
        u.setActive(active);
        u = users.save(u);
        logs.log("Created " + role.getLabel() + " account '" + name + "'");
        return u;
    }

    @Transactional
    public AppUser update(Long id, String newPassword, Role role, Long memberId, boolean active, String currentUsername) {
        AppUser u = get(id);
        if (role == null) throw new LibraryException("Please choose a role.");
        boolean self = u.getUsername().equals(currentUsername);
        if (self && (role != u.getRole() || !active))
            throw new LibraryException("You can't change your own role or disable your own account.");
        Member member = resolveMember(role, memberId, u.getId());
        boolean resetPassword = newPassword != null && !newPassword.isBlank();
        if (resetPassword) checkPassword(newPassword);

        u.setRole(role);
        u.setMember(member);
        u.setActive(active);
        if (resetPassword) u.setPasswordHash(encoder.encode(newPassword));
        u = users.save(u);
        logs.log("Updated account '" + u.getUsername() + "'" + (resetPassword ? " (password reset)" : ""));
        return u;
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        AppUser u = get(id);
        if (u.getUsername().equals(currentUsername))
            throw new LibraryException("You can't delete your own account.");
        users.delete(u);
        logs.log("Deleted account '" + u.getUsername() + "'");
    }

    private void checkPassword(String password) {
        if (password == null || password.length() < 6)
            throw new LibraryException("Password must be at least 6 characters.");
    }

    /** Teacher/Student accounts must be linked to a matching member record; staff accounts have none. */
    private Member resolveMember(Role role, Long memberId, Long ownUserId) {
        if (role == Role.ADMIN || role == Role.LIBRARIAN) return null;
        if (memberId == null) throw new LibraryException("Choose the member record this account belongs to.");
        Member m = members.findById(memberId).orElseThrow(() -> new LibraryException("Member not found."));
        MemberType expected = role == Role.TEACHER ? MemberType.TEACHER : MemberType.STUDENT;
        if (m.getMemberType() != expected)
            throw new LibraryException("A " + role.getLabel() + " account must be linked to a " + expected.getLabel() + " member.");
        var existing = users.findByMemberId(memberId);
        if (existing.isPresent() && !existing.get().getId().equals(ownUserId))
            throw new LibraryException("That member already has a login account.");
        return m;
    }
}
