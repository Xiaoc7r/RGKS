package com.jlu.registration.module.teaching;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByStudentIdAndOfferingId(Long studentId, Long offeringId);

    List<Grade> findByStudentIdOrderBySemesterDesc(Long studentId);

    List<Grade> findByOfferingId(Long offeringId);

    @Query("select case when count(g) > 0 then true else false end from Grade g "
            + "where g.studentId = :studentId and g.courseId = :courseId "
            + "and upper(g.gradeValue) in ('A', 'B', 'C', 'D')")
    boolean existsPassingGrade(@Param("studentId") Long studentId,
                               @Param("courseId") Long courseId);
}

