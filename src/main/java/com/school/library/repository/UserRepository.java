package com.school.library.repository;

import com.school.library.model.AppUser;
import com.school.library.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<AppUser> findByMemberId(Long memberId);
    List<AppUser> findByRoleOrderByUsernameAsc(Role role);
}
