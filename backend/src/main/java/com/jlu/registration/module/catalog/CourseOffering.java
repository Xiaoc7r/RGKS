package com.jlu.registration.module.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_offerings")
@Getter
@Setter
@NoArgsConstructor
public class CourseOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 30)
    private String semester;

    private Long professorId;

    @Column(nullable = false)
    private Integer dayOfWeek;

    @Column(nullable = false)
    private Integer startPeriod;

    @Column(nullable = false)
    private Integer endPeriod;

    @Column(nullable = false)
    private Integer capacity = 10;

    @Column(nullable = false)
    private Integer minimumEnrollment = 3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferingStatus status = OfferingStatus.OPEN;

    public CourseOffering(Long courseId, String semester, Long professorId,
                          Integer dayOfWeek, Integer startPeriod, Integer endPeriod) {
        this.courseId = courseId;
        this.semester = semester;
        this.professorId = professorId;
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    public boolean conflictsWith(CourseOffering other) {
        return semester.equals(other.semester)
                && dayOfWeek.equals(other.dayOfWeek)
                && startPeriod <= other.endPeriod
                && other.startPeriod <= endPeriod;
    }
}

