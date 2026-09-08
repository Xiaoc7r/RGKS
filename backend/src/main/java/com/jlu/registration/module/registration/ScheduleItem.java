package com.jlu.registration.module.registration;

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
@Table(name = "schedule_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "offering_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "offering_id", nullable = false)
    private Long offeringId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChoiceType choiceType;

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.SELECTED;

    public ScheduleItem(Long scheduleId, Long offeringId, ChoiceType choiceType, Integer priority) {
        this.scheduleId = scheduleId;
        this.offeringId = offeringId;
        this.choiceType = choiceType;
        this.priority = priority;
    }
}

