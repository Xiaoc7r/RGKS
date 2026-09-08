package com.jlu.registration.module.operations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {
    List<BillingRecord> findBySemesterOrderByStudentIdAsc(String semester);
    Optional<BillingRecord> findByStudentIdAndSemester(Long studentId, String semester);
}

