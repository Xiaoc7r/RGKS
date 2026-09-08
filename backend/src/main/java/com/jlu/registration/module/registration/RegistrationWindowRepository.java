package com.jlu.registration.module.registration;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationWindowRepository extends JpaRepository<RegistrationWindow, Long> {
    Optional<RegistrationWindow> findBySemester(String semester);
}

