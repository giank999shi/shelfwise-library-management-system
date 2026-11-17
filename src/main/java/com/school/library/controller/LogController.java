package com.school.library.controller;

import com.school.library.service.ActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogController {

    private final ActivityLogService logs;

    public LogController(ActivityLogService logs) { this.logs = logs; }

    @GetMapping("/admin/logs")
    public String logs(Model model) {
        model.addAttribute("logs", logs.recent());
        return "logs";
    }
}
