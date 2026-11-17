package com.school.library.service;

import com.school.library.model.LoanStatus;
import com.school.library.model.Member;
import com.school.library.model.MemberType;
import com.school.library.repository.LoanRepository;
import com.school.library.repository.MemberRepository;
import com.school.library.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository members;
    private final LoanRepository loans;
    private final UserRepository users;
    private final ActivityLogService logs;

    public MemberService(MemberRepository members, LoanRepository loans, UserRepository users, ActivityLogService logs) {
        this.members = members; this.loans = loans; this.users = users; this.logs = logs;
    }

    public List<Member> list(String keyword) {
        if (keyword == null || keyword.isBlank()) return members.findAll(Sort.by("id"));
        return members.findByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim());
    }

    public List<Member> all() { return members.findAll(Sort.by("name")); }

    public Member get(Long id) {
        return members.findById(id).orElseThrow(() -> new LibraryException("Member not found."));
    }

    /** Flow E: add or edit a member (validate, save, log). */
    @Transactional
    public Member save(Long id, String name, MemberType type, boolean active) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) throw new LibraryException("Name is required.");
        if (n.length() > 100) throw new LibraryException("Name is too long (max 100 characters).");
        if (type == null) throw new LibraryException("Please choose Student or Teacher.");

        Member m;
        boolean isNew = id == null;
        if (isNew) {
            m = new Member();
        } else {
            m = get(id);
            if (m.getMemberType() != type && loans.countByMemberIdAndStatus(id, LoanStatus.BORROWED) > 0)
                throw new LibraryException("Can't change the type while this member has borrowed books.");
        }
        m.setName(n);
        m.setMemberType(type);
        m.setActive(active);
        m = members.save(m);
        logs.log((isNew ? "Added " : "Edited ") + type.getLabel().toLowerCase() + " member #" + m.getId() + " '" + n + "'");
        return m;
    }

    @Transactional
    public void delete(Long id) {
        Member m = get(id);
        if (loans.countByMemberIdAndStatus(id, LoanStatus.BORROWED) > 0)
            throw new LibraryException("Cannot delete: this member still has borrowed books.");
        if (loans.existsByMemberId(id))
            throw new LibraryException("Cannot delete: this member has borrowing history. Mark the member inactive instead.");
        if (users.findByMemberId(id).isPresent())
            throw new LibraryException("Cannot delete: this member has a login account. Delete the account first.");
        members.delete(m);
        logs.log("Deleted member #" + id + " '" + m.getName() + "'");
    }
}
