package com.school.library.config;

import com.school.library.model.*;
import com.school.library.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DEMO DATA so you can try every role right away.
 * Runs only when the users table is empty. Delete this class (or change the
 * passwords) before using the system for real.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final MemberRepository members;
    private final BookRepository books;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, MemberRepository members, BookRepository books, PasswordEncoder encoder) {
        this.users = users; this.members = members; this.books = books; this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        Member student = member("Demo Student", MemberType.STUDENT);
        Member teacher = member("Demo Teacher", MemberType.TEACHER);

        user("admin", "admin123", Role.ADMIN, null);
        user("librarian", "librarian123", Role.LIBRARIAN, null);
        user("teacher", "teacher123", Role.TEACHER, teacher);
        user("student", "student123", Role.STUDENT, student);

        if (books.count() == 0) {
            book("Noli Me Tangere", "Jose Rizal", 3);
            book("El Filibusterismo", "Jose Rizal", 2);
            book("Introduction to Algorithms", "Thomas H. Cormen", 2);
            book("Clean Code", "Robert C. Martin", 1);
        }
    }

    private Member member(String name, MemberType type) {
        Member m = new Member();
        m.setName(name);
        m.setMemberType(type);
        return members.save(m);
    }

    private void user(String username, String password, Role role, Member member) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        u.setMember(member);
        users.save(u);
    }

    private void book(String title, String author, int copies) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        books.save(b);
    }
}
