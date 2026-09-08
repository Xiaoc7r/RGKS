package com.jlu.registration.module.catalog;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {

    List<CourseOffering> findBySemesterOrderByDayOfWeekAscStartPeriodAsc(String semester);

    List<CourseOffering> findByProfessorIdAndSemesterOrderByDayOfWeekAscStartPeriodAsc(
            Long professorId, String semester);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CourseOffering o where o.id = :id")
    Optional<CourseOffering> findByIdForUpdate(@Param("id") Long id);
}

