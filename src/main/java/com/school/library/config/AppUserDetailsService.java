package com.school.library.config;

import com.school.library.model.AppUser;
import com.school.library.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public AppUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login"));
        return User.withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles(u.getRole().name())   // becomes ROLE_ADMIN, ROLE_LIBRARIAN, ...
                .disabled(!u.isActive())
                .build();
    }
}
