package com.jlu.registration.module.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jlu.registration.module.catalog.Course;
import com.jlu.registration.module.catalog.CourseOffering;
import com.jlu.registration.module.catalog.CourseOfferingRepository;
import com.jlu.registration.module.catalog.CourseRepository;
import com.jlu.registration.module.catalog.OfferingStatus;
import com.jlu.registration.module.identity.CurrentUserService;
import com.jlu.registration.module.grading.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RegistrationService {

    private final StudentScheduleRepository schedules;
    private final ScheduleItemRepository items;
    private final RegistrationWindowRepository windows;
    private final CourseOfferingRepository offerings;
    private final CourseRepository courses;
    private final GradeRepository grades;
    private final CurrentUserService currentUser;

    public RegistrationService(StudentScheduleRepository schedules,
                               ScheduleItemRepository items,
                               RegistrationWindowRepository windows,
                               CourseOfferingRepository offerings,
                               CourseRepository courses,
                               GradeRepository grades,
                               CurrentUserService currentUser) {
        this.schedules = schedules;
        this.items = items;
        this.windows = windows;
        this.offerings = offerings;
        this.courses = courses;
        this.grades = grades;
        this.currentUser = currentUser;
    }

    public RegistrationController.ScheduleView mySchedule(String semester) {
        Long studentId = currentUser.requireLinkedPersonId();
        return schedules.findByStudentIdAndSemester(studentId, semester)
                .map(this::toView)
                .orElse(new RegistrationController.ScheduleView(null, semester, ScheduleStatus.DRAFT, List.of()));
    }

    @Transactional
    public RegistrationController.ScheduleView addSelection(RegistrationController.SelectionRequest request) {
        requireOpen(request.semester());
        validatePriority(request.choiceType(), request.priority());
        Long studentId = currentUser.requireLinkedPersonId();
        StudentSchedule schedule = schedules.findByStudentIdAndSemester(studentId, request.semester())
                .orElseGet(() -> schedules.save(new StudentSchedule(studentId, request.semester())));
        if (items.existsByScheduleIdAndOfferingId(schedule.getId(), request.offeringId())) {
            throw new IllegalArgumentException("该课程已在课表中");
        }
        if (items.existsByScheduleIdAndChoiceTypeAndPriority(
                schedule.getId(), request.choiceType(), request.priority())) {
            throw new IllegalArgumentException("同一选课类型不能重复使用相同优先级");
        }
        long limit = request.choiceType() == ChoiceType.PRIMARY ? 4 : 2;
        if (items.countByScheduleIdAndChoiceType(schedule.getId(), request.choiceType()) >= limit) {
            throw new IllegalStateException(request.choiceType() == ChoiceType.PRIMARY
                    ? "主选课程最多 4 门" : "备选课程最多 2 门");
        }
        CourseOffering offering = offerings.findById(request.offeringId())
                .orElseThrow(() -> new IllegalArgumentException("教学班不存在"));
        if (!offering.getSemester().equals(request.semester()) || offering.getStatus() != OfferingStatus.OPEN) {
            throw new IllegalStateException("该教学班当前不可选");
        }
        items.save(new ScheduleItem(schedule.getId(), offering.getId(),
                request.choiceType(), request.priority()));
        markDraft(schedule);
        return toView(schedule);
    }

    @Transactional
    public void removeSelection(Long itemId, String semester) {
        requireOpen(semester);
        Long studentId = currentUser.requireLinkedPersonId();
        StudentSchedule schedule = schedules.findByStudentIdAndSemester(studentId, semester)
                .orElseThrow(() -> new IllegalArgumentException("课表不存在"));
        ScheduleItem item = items.findByIdAndScheduleId(itemId, schedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        items.delete(item);
        markDraft(schedule);
    }

    @Transactional
    public RegistrationController.ScheduleView updateSelection(
            Long itemId, String semester, RegistrationController.UpdateSelectionRequest request) {
        requireOpen(semester);
        validatePriority(request.choiceType(), request.priority());
        Long studentId = currentUser.requireLinkedPersonId();
        StudentSchedule schedule = schedules.findByStudentIdAndSemester(studentId, semester)
                .orElseThrow(() -> new IllegalArgumentException("课表不存在"));
        ScheduleItem item = items.findByIdAndScheduleId(itemId, schedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        List<ScheduleItem> others = items.findByScheduleIdOrderByChoiceTypeAscPriorityAsc(schedule.getId())
                .stream().filter(value -> !value.getId().equals(itemId)).toList();
        if (others.stream().anyMatch(value -> value.getChoiceType() == request.choiceType()
                && value.getPriority().equals(request.priority()))) {
            throw new IllegalArgumentException("同一选课类型不能重复使用相同优先级");
        }
        long countOfType = others.stream()
                .filter(value -> value.getChoiceType() == request.choiceType()).count();
        long limit = request.choiceType() == ChoiceType.PRIMARY ? 4 : 2;
        if (countOfType >= limit) {
            throw new IllegalStateException(request.choiceType() == ChoiceType.PRIMARY
                    ? "主选课程最多 4 门" : "备选课程最多 2 门");
        }
        item.setChoiceType(request.choiceType());
        item.setPriority(request.priority());
        markDraft(schedule);
        return toView(schedule);
    }

    @Transactional
    public RegistrationController.ScheduleView saveDraft(String semester) {
        requireOpen(semester);
        Long studentId = currentUser.requireLinkedPersonId();
        StudentSchedule schedule = schedules.findByStudentIdAndSemester(studentId, semester)
                .orElseThrow(() -> new IllegalStateException("请先创建课表"));
        markDraft(schedule);
        return toView(schedule);
    }

    @Transactional
    public void deleteSchedule(String semester) {
        requireOpen(semester);
        Long studentId = currentUser.requireLinkedPersonId();
        StudentSchedule schedule = schedules.findByStudentIdAndSemester(studentId, semester)
                .orElseThrow(() -> new IllegalArgumentException("课表不存在"));
        items.deleteByScheduleId(schedule.getId());
        schedules.delete(schedule);
    }

    @Transactional
    public RegistrationController.ScheduleView submit(String semester) {
        requireOpen(semester);
        Long studentId = currentUser.requireLinkedPersonId();
        StudentSchedule schedule = schedules.findByStudentIdAndSemester(studentId, semester)
                .orElseThrow(() -> new IllegalStateException("请先创建课表"));
        List<ScheduleItem> allItems = items.findByScheduleIdOrderByChoiceTypeAscPriorityAsc(schedule.getId());
        long primaryCount = allItems.stream().filter(item -> item.getChoiceType() == ChoiceType.PRIMARY).count();
        long alternateCount = allItems.stream().filter(item -> item.getChoiceType() == ChoiceType.ALTERNATE).count();
        if (primaryCount != 4 || alternateCount != 2) {
            throw new IllegalStateException("提交前必须选择 4 门主选课和 2 门备选课");
        }

        List<CourseOffering> selectedOfferings = new ArrayList<>();
        for (ScheduleItem item : allItems.stream()
                .filter(value -> value.getChoiceType() == ChoiceType.PRIMARY)
                .toList()) {
            CourseOffering offering = offerings.findByIdForUpdate(item.getOfferingId())
                    .orElseThrow(() -> new IllegalArgumentException("教学班不存在"));
            validateOffering(studentId, offering, selectedOfferings);
            selectedOfferings.add(offering);
        }
        allItems.forEach(item -> item.setStatus(item.getChoiceType() == ChoiceType.PRIMARY
                ? EnrollmentStatus.ENROLLED : EnrollmentStatus.SELECTED));
        schedule.setStatus(ScheduleStatus.SUBMITTED);
        return toView(schedule);
    }

    private void validateOffering(Long studentId, CourseOffering offering,
                                  List<CourseOffering> selectedOfferings) {
        if (offering.getStatus() != OfferingStatus.OPEN) {
            throw new IllegalStateException(courseLabel(offering) + " 已关闭");
        }
        if (items.countEnrolledByOfferingId(offering.getId()) >= offering.getCapacity()) {
            throw new IllegalStateException(courseLabel(offering) + " 已满，请重新选择后再提交");
        }
        if (selectedOfferings.stream().anyMatch(offering::conflictsWith)) {
            throw new IllegalStateException("主选课程存在时间冲突");
        }
        Course course = courses.findById(offering.getCourseId())
                .orElseThrow(() -> new IllegalStateException("课程目录数据缺失"));
        if (course.getPrerequisiteCourseId() != null
                && !grades.existsPassingGrade(studentId, course.getPrerequisiteCourseId())) {
            throw new IllegalStateException("未满足课程 " + course.getCode() + " 的先修要求");
        }
    }

    private void requireOpen(String semester) {
        RegistrationWindow window = windows.findBySemester(semester)
                .orElseThrow(() -> new IllegalStateException("当前学期未配置选课窗口"));
        if (!window.isOpen()) {
            throw new IllegalStateException("当前学期选课已关闭");
        }
    }

    private void validatePriority(ChoiceType choiceType, Integer priority) {
        int maxPriority = choiceType == ChoiceType.PRIMARY ? 4 : 2;
        if (priority < 1 || priority > maxPriority) {
            throw new IllegalArgumentException(choiceType == ChoiceType.PRIMARY
                    ? "主选优先级必须为 1-4" : "备选优先级必须为 1-2");
        }
    }

    /** 修改已提交课表时释放原占位，重新提交后才再次计入容量。 */
    private void markDraft(StudentSchedule schedule) {
        schedule.setStatus(ScheduleStatus.DRAFT);
        items.findByScheduleIdOrderByChoiceTypeAscPriorityAsc(schedule.getId())
                .forEach(item -> item.setStatus(EnrollmentStatus.SELECTED));
    }

    private String courseLabel(CourseOffering offering) {
        return courses.findById(offering.getCourseId())
                .map(course -> "课程 " + course.getCode() + " " + course.getName())
                .orElse("教学班 " + offering.getId());
    }

    private RegistrationController.ScheduleView toView(StudentSchedule schedule) {
        List<ScheduleItem> scheduleItems = items.findByScheduleIdOrderByChoiceTypeAscPriorityAsc(schedule.getId());
        Map<Long, CourseOffering> offeringMap = offerings.findAllById(
                        scheduleItems.stream().map(ScheduleItem::getOfferingId).toList()).stream()
                .collect(Collectors.toMap(CourseOffering::getId, Function.identity()));
        Map<Long, Course> courseMap = courses.findAllById(
                        offeringMap.values().stream().map(CourseOffering::getCourseId).toList()).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        List<RegistrationController.ScheduleItemView> views = scheduleItems.stream().map(item -> {
            CourseOffering offering = offeringMap.get(item.getOfferingId());
            Course course = courseMap.get(offering.getCourseId());
            return new RegistrationController.ScheduleItemView(
                    item.getId(), offering.getId(), course.getCode(), course.getName(),
                    item.getChoiceType(), item.getPriority(), item.getStatus(),
                    offering.getDayOfWeek(), offering.getStartPeriod(), offering.getEndPeriod());
        }).toList();
        return new RegistrationController.ScheduleView(
                schedule.getId(), schedule.getSemester(), schedule.getStatus(), views);
    }
}
