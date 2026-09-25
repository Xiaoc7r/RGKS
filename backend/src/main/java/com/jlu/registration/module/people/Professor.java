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
@Table(name = "professors", uniqueConstraints = @UniqueConstraint(columnNames = "employee_number"))
@Getter
@Setter
@NoArgsConstructor
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_number", nullable = false, length = 30)
    private String employeeNumber;

    @Column(nullable = false, length = 80)
    private String name;

    private LocalDate dateOfBirth;

    @Column(length = 32)
    private String identityNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PersonStatus status = PersonStatus.ACTIVE;

    @Column(nullable = false, length = 100)
    private String department;

    public Professor(String employeeNumber, String name, LocalDate dateOfBirth,
                     String identityNumber, PersonStatus status, String department) {
        this.employeeNumber = employeeNumber;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.identityNumber = identityNumber;
        this.status = status;
        this.department = department;
    }
}
