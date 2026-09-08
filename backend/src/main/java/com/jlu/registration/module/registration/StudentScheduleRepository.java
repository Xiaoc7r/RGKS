package com.jlu.registration.module.registration;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentScheduleRepository extends JpaRepository<StudentSchedule, Long> {
    Optional<StudentSchedule> findByStudentIdAndSemester(Long studentId, String semester);
    List<StudentSchedule> findBySemester(String semester);
}

