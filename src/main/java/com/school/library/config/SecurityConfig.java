package com.school.library.config;

import com.school.library.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, ActivityLogService logs, AppUserDetailsService userDetails,
                                    @Value("${app.remember-me-key:#{T(java.util.UUID).randomUUID().toString()}}") String rememberKey)
            throws Exception {
        http
          .authorizeHttpRequests(a -> a
              .requestMatchers("/login", "/forgot-password", "/css/**").permitAll()
              // Admin only: user accounts, librarians, activity logs
              .requestMatchers("/admin/**").hasRole("ADMIN")
              // Admin + Librarian: books, members, borrowing, returning, overdue, history, reports
              .requestMatchers("/staff/**").hasAnyRole("ADMIN", "LIBRARIAN")
              // Everyone logged in: search books, own history
              .requestMatchers("/books/search", "/my/**").authenticated()
              .anyRequest().authenticated())
          .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
          .rememberMe(r -> r.key(rememberKey).userDetailsService(userDetails).tokenValiditySeconds(14 * 24 * 60 * 60))
          .logout(l -> l
              .logoutSuccessUrl("/login?logout")
              .addLogoutHandler((request, response, authentication) -> {
                  if (authentication != null) logs.logAs(authentication.getName(), "Logged out");
              }));
        return http.build();
    }
}
