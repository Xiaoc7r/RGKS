package com.jlu.registration.module.people;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(columnNames = "student_number"))
@Getter
@Setter
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", nullable = false, length = 30)
    private String studentNumber;

    @Column(nullable = false, length = 80)
    private String name;

    private LocalDate dateOfBirth;

    @Column(length = 32)
    private String identityNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PersonStatus status = PersonStatus.ACTIVE;

    @Column(nullable = false, length = 100)
    private String major;

    private LocalDate graduationDate;

    public Student(String studentNumber, String name, LocalDate dateOfBirth,
                   String identityNumber, PersonStatus status,
                   String major, LocalDate graduationDate) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.identityNumber = identityNumber;
        this.status = status;
        this.major = major;
        this.graduationDate = graduationDate;
    }
}
