package com.school.library.service;

import com.school.library.model.*;
import com.school.library.repository.LoanRepository;
import com.school.library.repository.MemberRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Flow K: builds the five report types and turns any report into CSV. */
@Service
public class ReportService {

    private final LoanRepository loans;
    private final MemberRepository members;

    public ReportService(LoanRepository loans, MemberRepository members) {
        this.loans = loans; this.members = members;
    }

    public Report build(ReportType type) {
        return switch (type) {
            case MOST_BORROWED -> mostBorrowed(type);
            case MONTHLY -> monthly(type);
            case OVERDUE -> overdue(type);
            case MEMBER -> memberReport(type);
            case RETURNED -> returned(type);
        };
    }

    private Report mostBorrowed(ReportType type) {
        Map<Long, List<Loan>> byBook = new LinkedHashMap<>();
        for (Loan l : loans.findAll()) byBook.computeIfAbsent(l.getBook().getId(), k -> new ArrayList<>()).add(l);

        List<List<Loan>> groups = new ArrayList<>(byBook.values());
        groups.sort((a, b) -> b.size() != a.size()
                ? Integer.compare(b.size(), a.size())
                : a.get(0).getBook().getTitle().compareToIgnoreCase(b.get(0).getBook().getTitle()));

        List<List<String>> rows = new ArrayList<>();
        int rank = 1;
        for (List<Loan> g : groups) {
            Book b = g.get(0).getBook();
            long now = g.stream().filter(x -> x.getStatus() == LoanStatus.BORROWED).count();
            rows.add(row(rank++, b.getTitle(), b.getAuthor(), g.size(), now));
        }
        return new Report(type.getTitle(), type.getDescription(),
                List.of("Rank", "Title", "Author", "Times borrowed", "Borrowed now"), rows);
    }

    private Report monthly(ReportType type) {
        TreeMap<YearMonth, int[]> months = new TreeMap<>(Comparator.reverseOrder());
        for (Loan l : loans.findAll()) {
            months.computeIfAbsent(YearMonth.from(l.getBorrowDate()), k -> new int[2])[0]++;
            if (l.getReturnDate() != null)
                months.computeIfAbsent(YearMonth.from(l.getReturnDate()), k -> new int[2])[1]++;
        }
        List<List<String>> rows = new ArrayList<>();
        for (Map.Entry<YearMonth, int[]> e : months.entrySet())
            rows.add(row(e.getKey(), e.getValue()[0], e.getValue()[1]));
        return new Report(type.getTitle(), type.getDescription(),
                List.of("Month", "Books borrowed", "Books returned"), rows);
    }

    private Report overdue(ReportType type) {
        LocalDate today = LocalDate.now();
        List<Loan> list = new ArrayList<>(loans.findByStatusAndDueDateBefore(LoanStatus.BORROWED, today));
        list.sort(Comparator.comparing(Loan::getDueDate));
        List<List<String>> rows = new ArrayList<>();
        for (Loan l : list)
            rows.add(row(l.getMember().getName(), l.getMember().getMemberType().getLabel(), l.getBook().getTitle(),
                    l.getBorrowDate(), l.getDueDate(), ChronoUnit.DAYS.between(l.getDueDate(), today)));
        return new Report(type.getTitle(), type.getDescription(),
                List.of("Member", "Type", "Book", "Borrowed", "Due date", "Days overdue"), rows);
    }

    private Report memberReport(ReportType type) {
        Map<Long, List<Loan>> byMember = new HashMap<>();
        for (Loan l : loans.findAll()) byMember.computeIfAbsent(l.getMember().getId(), k -> new ArrayList<>()).add(l);

        List<List<String>> rows = new ArrayList<>();
        for (Member m : members.findAll(Sort.by("name"))) {
            List<Loan> g = byMember.getOrDefault(m.getId(), List.of());
            long current = g.stream().filter(x -> x.getStatus() == LoanStatus.BORROWED).count();
            long late = g.stream().filter(Loan::isOverdue).count();
            rows.add(row(m.getId(), m.getName(), m.getMemberType().getLabel(),
                    m.isActive() ? "Active" : "Inactive", g.size(), current, late));
        }
        return new Report(type.getTitle(), type.getDescription(),
                List.of("Member ID", "Name", "Type", "Status", "Total borrowed", "Borrowed now", "Overdue now"), rows);
    }

    private Report returned(ReportType type) {
        List<List<String>> rows = new ArrayList<>();
        for (Loan l : loans.findByStatusOrderByReturnDateDesc(LoanStatus.RETURNED)) {
            String timing = l.getReturnDate().isAfter(l.getDueDate())
                    ? "Late (" + ChronoUnit.DAYS.between(l.getDueDate(), l.getReturnDate()) + " day(s))"
                    : "On time";
            rows.add(row(l.getMember().getName(), l.getBook().getTitle(),
                    l.getBorrowDate(), l.getDueDate(), l.getReturnDate(), timing));
        }
        return new Report(type.getTitle(), type.getDescription(),
                List.of("Member", "Book", "Borrowed", "Due date", "Returned", "Result"), rows);
    }

    private List<String> row(Object... cells) {
        List<String> r = new ArrayList<>();
        for (Object c : cells) r.add(String.valueOf(c));
        return r;
    }

    // ----- CSV export (opens correctly in Excel, including names like "Niño") -----

    public String toCsv(Report report) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        appendRow(sb, report.getHeaders());
        for (List<String> row : report.getRows()) appendRow(sb, row);
        return sb.toString();
    }

    private void appendRow(StringBuilder sb, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(csv(cells.get(i)));
        }
        sb.append("\r\n");
    }

    private String csv(String v) {
        if (v == null) return "";
        // stop spreadsheet formulas (=, +, -, @) from running if a title starts with them
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r"))
            return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }
}
