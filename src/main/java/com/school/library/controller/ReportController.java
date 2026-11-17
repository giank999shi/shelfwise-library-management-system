package com.school.library.controller;

import com.school.library.service.ActivityLogService;
import com.school.library.service.Report;
import com.school.library.service.ReportService;
import com.school.library.service.ReportType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/staff/reports")
public class ReportController {

    private final ReportService reports;
    private final ActivityLogService logs;

    public ReportController(ReportService reports, ActivityLogService logs) {
        this.reports = reports; this.logs = logs;
    }

    /** K1-K2: pick a report type. */
    @GetMapping
    public String menu(Model model) {
        model.addAttribute("types", ReportType.values());
        return "reports";
    }

    /** K3-K5: retrieve, generate and display. The page has Print and Export CSV buttons (K6). */
    @GetMapping("/{type}")
    public String view(@PathVariable("type") String slug, Model model) {
        ReportType type = ReportType.fromSlug(slug);
        Report report = reports.build(type);
        logs.log("Generated report: " + type.getTitle());
        model.addAttribute("report", report);
        model.addAttribute("type", type);
        return "report";
    }

    @GetMapping("/{type}/csv")
    public ResponseEntity<byte[]> csv(@PathVariable("type") String slug) {
        ReportType type = ReportType.fromSlug(slug);
        byte[] body = reports.toCsv(reports.build(type)).getBytes(StandardCharsets.UTF_8);
        logs.log("Exported report to CSV: " + type.getTitle());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report-" + type.getSlug() + "-" + LocalDate.now() + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
