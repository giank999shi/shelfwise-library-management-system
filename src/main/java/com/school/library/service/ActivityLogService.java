package com.school.library.service;

import com.school.library.model.ActivityLog;
import com.school.library.repository.ActivityLogRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

/** Writes the Activity Logs table. Every important action calls log(...). */
@Service
public class ActivityLogService {

    private final ActivityLogRepository repo;

    public ActivityLogService(ActivityLogRepository repo) { this.repo = repo; }

    /** Logs an action by whoever is currently logged in. */
    public void log(String action) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        boolean real = a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);
        logAs(real ? a.getName() : "system", action);
    }

    public void logAs(String username, String action) {
        ActivityLog l = new ActivityLog();
        l.setUsername(cut(username, 50));
        l.setAction(cut(action, 255));
        repo.save(l);
    }

    public List<ActivityLog> recent() { return repo.findTop200ByOrderByCreatedAtDescIdDesc(); }

    private String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
