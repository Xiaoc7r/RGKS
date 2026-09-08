package com.jlu.registration.module.operations;

import java.math.BigDecimal;
import java.time.Instant;

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
@Table(name = "billing_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "semester"}))
@Getter
@Setter
@NoArgsConstructor
public class BillingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 30)
    private String semester;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingStatus status = BillingStatus.PENDING;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private Instant lastAttemptAt;

    public BillingRecord(Long studentId, String semester, BigDecimal amount) {
        this.studentId = studentId;
        this.semester = semester;
        this.amount = amount;
    }
}

