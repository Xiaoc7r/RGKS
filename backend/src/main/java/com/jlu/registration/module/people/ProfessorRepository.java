package com.jlu.registration.module.people;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByEmployeeNumber(String employeeNumber);
    boolean existsByEmployeeNumber(String employeeNumber);
    boolean existsByEmployeeNumberAndIdNot(String employeeNumber, Long id);
}
