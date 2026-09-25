package com.jlu.registration.module.registration;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

    List<ScheduleItem> findByScheduleIdOrderByChoiceTypeAscPriorityAsc(Long scheduleId);

    List<ScheduleItem> findByScheduleIdAndStatus(Long scheduleId, EnrollmentStatus status);

    Optional<ScheduleItem> findByIdAndScheduleId(Long id, Long scheduleId);

    boolean existsByScheduleIdAndOfferingId(Long scheduleId, Long offeringId);

    boolean existsByScheduleIdAndChoiceTypeAndPriority(
            Long scheduleId, ChoiceType choiceType, Integer priority);

    long countByScheduleIdAndChoiceType(Long scheduleId, ChoiceType choiceType);

    @Query("select count(i) from ScheduleItem i where i.offeringId = :offeringId and i.status = com.jlu.registration.module.registration.EnrollmentStatus.ENROLLED")
    long countEnrolledByOfferingId(@Param("offeringId") Long offeringId);

    @Query("select i from ScheduleItem i where i.offeringId = :offeringId and i.status = com.jlu.registration.module.registration.EnrollmentStatus.ENROLLED")
    List<ScheduleItem> findEnrolledByOfferingId(@Param("offeringId") Long offeringId);

    List<ScheduleItem> findByOfferingIdInAndStatus(Collection<Long> offeringIds, EnrollmentStatus status);

    void deleteByScheduleId(Long scheduleId);
}
