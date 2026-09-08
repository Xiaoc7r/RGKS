package com.jlu.registration.module.catalog;

import java.math.BigDecimal;

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
@Table(name = "courses", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer credits;

    private Long prerequisiteCourseId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tuition;

    public Course(String code, String name, Integer credits,
                  Long prerequisiteCourseId, BigDecimal tuition) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.prerequisiteCourseId = prerequisiteCourseId;
        this.tuition = tuition;
    }
}

