package com.jlu.registration.module.operations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jlu.registration.module.catalog.Course;
import com.jlu.registration.module.catalog.CourseOffering;
import com.jlu.registration.module.catalog.CourseOfferingRepository;
import com.jlu.registration.module.catalog.CourseRepository;
import com.jlu.registration.module.catalog.OfferingStatus;
import com.jlu.registration.module.registration.ChoiceType;
import com.jlu.registration.module.registration.EnrollmentStatus;
import com.jlu.registration.module.registration.RegistrationWindow;
import com.jlu.registration.module.registration.RegistrationWindowRepository;
import com.jlu.registration.module.registration.ScheduleItem;
import com.jlu.registration.module.registration.ScheduleItemRepository;
import com.jlu.registration.module.registration.ScheduleStatus;
import com.jlu.registration.module.registration.StudentSchedule;
import com.jlu.registration.module.registration.StudentScheduleRepository;
import com.jlu.registration.module.teaching.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OperationsService {

    private final RegistrationWindowRepository windows;
    private final CourseOfferingRepository offerings;
    private final CourseRepository courses;
    private final StudentScheduleRepository schedules;
    private final ScheduleItemRepository items;
    private final GradeRepository grades;
    private final BillingRecordRepository billings;

    public OperationsService(RegistrationWindowRepository windows,
                             CourseOfferingRepository offerings,
                             CourseRepository courses,
                             StudentScheduleRepository schedules,
                             ScheduleItemRepository items,
                             GradeRepository grades,
                             BillingRecordRepository billings) {
        this.windows = windows;
        this.offerings = offerings;
        this.courses = courses;
        this.schedules = schedules;
        this.items = items;
        this.grades = grades;
        this.billings = billings;
    }

    @Transactional
    public OperationsController.CloseSummary closeRegistration(String semester) {
        RegistrationWindow window = windows.findBySemester(semester)
                .orElseThrow(() -> new IllegalArgumentException("学期不存在"));
        if (!window.isOpen()) {
            throw new IllegalStateException("该学期已经关闭选课");
        }
        List<CourseOffering> semesterOfferings = offerings
                .findBySemesterOrderByDayOfWeekAscStartPeriodAsc(semester);
        int cancelledCount = 0;
        for (CourseOffering offering : semesterOfferings) {
            if (offering.getProfessorId() == null) {
                offering.setStatus(OfferingStatus.CANCELLED);
                items.findEnrolledByOfferingId(offering.getId())
                        .forEach(item -> item.setStatus(EnrollmentStatus.CANCELLED));
                cancelledCount++;
            }
        }

        int promotedAlternates = 0;
        List<StudentSchedule> semesterSchedules = schedules.findBySemester(semester);
        for (StudentSchedule schedule : semesterSchedules) {
            promotedAlternates += levelSchedule(schedule);
        }
        for (CourseOffering offering : semesterOfferings) {
            if (offering.getStatus() != OfferingStatus.CANCELLED
                    && items.countEnrolledByOfferingId(offering.getId()) < offering.getMinimumEnrollment()) {
                offering.setStatus(OfferingStatus.CANCELLED);
                items.findEnrolledByOfferingId(offering.getId())
                        .forEach(item -> item.setStatus(EnrollmentStatus.CANCELLED));
                cancelledCount++;
            }
        }
        for (StudentSchedule schedule : semesterSchedules) {
            schedule.setStatus(ScheduleStatus.FINALIZED);
            createBilling(schedule);
        }
        semesterOfferings.stream()
                .filter(offering -> offering.getStatus() == OfferingStatus.OPEN)
                .forEach(offering -> offering.setStatus(OfferingStatus.CLOSED));
        window.setOpen(false);
        return new OperationsController.CloseSummary(
                semester, semesterOfferings.size(), cancelledCount,
                promotedAlternates, semesterSchedules.size());
    }

    public List<BillingRecord> listBilling(String semester) {
        return billings.findBySemesterOrderByStudentIdAsc(semester);
    }

    @Transactional
    public BillingRecord markBillingSent(Long billingId) {
        BillingRecord billing = billings.findById(billingId)
                .orElseThrow(() -> new IllegalArgumentException("计费记录不存在"));
        billing.setRetryCount(billing.getRetryCount() + 1);
        billing.setLastAttemptAt(Instant.now());
        billing.setStatus(BillingStatus.SENT);
        return billing;
    }

    private int levelSchedule(StudentSchedule schedule) {
        List<ScheduleItem> scheduleItems = items
                .findByScheduleIdOrderByChoiceTypeAscPriorityAsc(schedule.getId());
        List<CourseOffering> enrolled = new ArrayList<>();
        for (ScheduleItem item : scheduleItems) {
            if (item.getStatus() == EnrollmentStatus.ENROLLED) {
                CourseOffering offering = offerings.findById(item.getOfferingId()).orElseThrow();
                if (offering.getStatus() != OfferingStatus.CANCELLED) {
                    enrolled.add(offering);
                }
            }
        }
        int promoted = 0;
        for (ScheduleItem alternate : scheduleItems.stream()
                .filter(item -> item.getChoiceType() == ChoiceType.ALTERNATE)
                .sorted((left, right) -> left.getPriority().compareTo(right.getPriority()))
                .toList()) {
            if (enrolled.size() >= 4) {
                break;
            }
            CourseOffering candidate = offerings.findByIdForUpdate(alternate.getOfferingId()).orElseThrow();
            if (canPromote(schedule.getStudentId(), candidate, enrolled)) {
                alternate.setStatus(EnrollmentStatus.ENROLLED);
                enrolled.add(candidate);
                promoted++;
            }
        }
        return promoted;
    }

    private boolean canPromote(Long studentId, CourseOffering candidate,
                               List<CourseOffering> enrolled) {
        if (candidate.getStatus() != OfferingStatus.OPEN
                || candidate.getProfessorId() == null
                || items.countEnrolledByOfferingId(candidate.getId()) >= candidate.getCapacity()
                || enrolled.stream().anyMatch(candidate::conflictsWith)) {
            return false;
        }
        Course course = courses.findById(candidate.getCourseId()).orElseThrow();
        return course.getPrerequisiteCourseId() == null
                || grades.existsPassingGrade(studentId, course.getPrerequisiteCourseId());
    }

    private void createBilling(StudentSchedule schedule) {
        List<ScheduleItem> enrolledItems = items.findByScheduleIdAndStatus(
                schedule.getId(), EnrollmentStatus.ENROLLED);
        Set<Long> offeringIds = enrolledItems.stream()
                .map(ScheduleItem::getOfferingId)
                .collect(Collectors.toSet());
        Map<Long, CourseOffering> offeringMap = offerings.findAllById(offeringIds).stream()
                .collect(Collectors.toMap(CourseOffering::getId, Function.identity()));
        Set<Long> courseIds = offeringMap.values().stream()
                .map(CourseOffering::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, Course> courseMap = courses.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        BigDecimal amount = offeringMap.values().stream()
                .filter(offering -> offering.getStatus() != OfferingStatus.CANCELLED)
                .map(offering -> courseMap.get(offering.getCourseId()).getTuition())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BillingRecord billing = billings.findByStudentIdAndSemester(
                        schedule.getStudentId(), schedule.getSemester())
                .orElseGet(() -> new BillingRecord(
                        schedule.getStudentId(), schedule.getSemester(), amount));
        billing.setAmount(amount);
        billing.setStatus(BillingStatus.PENDING);
        billings.save(billing);
    }
}
