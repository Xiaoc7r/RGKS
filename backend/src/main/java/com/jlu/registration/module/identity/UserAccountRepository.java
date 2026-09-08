package com.jlu.registration.module.identity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByLinkedPersonIdAndRole(Long linkedPersonId, UserRole role);
}
