package com.school.library.config;

import com.school.library.service.ActivityLogService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** Writes login successes and failures to the Activity Logs. */
@Component
public class AuthEventLogger {

    private final ActivityLogService logs;

    public AuthEventLogger(ActivityLogService logs) { this.logs = logs; }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        logs.logAs(event.getAuthentication().getName(), "Logged in");
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        logs.logAs(String.valueOf(event.getAuthentication().getName()), "Failed login attempt");
    }
}
