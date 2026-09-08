package com.jlu.registration.module.teaching;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "offering_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "offering_id", nullable = false)
    private Long offeringId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(nullable = false, length = 30)
    private String semester;

    @Column(length = 2)
    private String gradeValue;

    public Grade(Long studentId, Long offeringId, Long courseId,
                 Long professorId, String semester, String gradeValue) {
        this.studentId = studentId;
        this.offeringId = offeringId;
        this.courseId = courseId;
        this.professorId = professorId;
        this.semester = semester;
        this.gradeValue = gradeValue;
    }
}

