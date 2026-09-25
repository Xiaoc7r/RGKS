package com.jlu.registration.module.grading;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.jlu.registration.module.people.Student;
import com.jlu.registration.module.people.StudentRepository;
import com.jlu.registration.module.registration.ScheduleItem;
import com.jlu.registration.module.registration.ScheduleItemRepository;
import com.jlu.registration.module.registration.StudentSchedule;
import com.jlu.registration.module.registration.StudentScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GradingService {

    private static final List<String> VALID_GRADES = List.of("A", "B", "C", "D", "F", "I");
    private static final Map<String, Integer> GRADE_POINTS = Map.of(
            "A", 4, "B", 3, "C", 2, "D", 1, "F", 0);

    private final CourseOfferingRepository offerings;
    private final CourseRepository courses;
    private final ScheduleItemRepository scheduleItems;
    private final StudentScheduleRepository schedules;
    private final StudentRepository students;
    private final GradeRepository grades;
    private final CurrentUserService currentUser;

    public GradingService(CourseOfferingRepository offerings,
                          CourseRepository courses,
                          ScheduleItemRepository scheduleItems,
                          StudentScheduleRepository schedules,
                          StudentRepository students,
                          GradeRepository grades,
                          CurrentUserService currentUser) {
        this.offerings = offerings;
        this.courses = courses;
        this.scheduleItems = scheduleItems;
        this.schedules = schedules;
        this.students = students;
        this.grades = grades;
        this.currentUser = currentUser;
    }

    public List<GradingController.GradingOfferingView> myOfferings(String semester) {
        Long professorId = currentUser.requireLinkedPersonId();
        Map<Long, Course> courseMap = courses.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return offerings.findByProfessorIdAndSemesterOrderByDayOfWeekAscStartPeriodAsc(
                        professorId, semester).stream()
                .map(offering -> {
                    Course course = courseMap.get(offering.getCourseId());
                    return new GradingController.GradingOfferingView(
                            offering.getId(), offering.getSemester(), course.getCode(), course.getName(),
                            scheduleItems.countEnrolledByOfferingId(offering.getId()), offering.getStatus());
                }).toList();
    }

    public List<GradingController.GradeRosterItemView> roster(Long offeringId) {
        Long professorId = currentUser.requireLinkedPersonId();
        CourseOffering offering = requireOwnedOffering(offeringId, professorId);
        Map<Long, StudentSchedule> scheduleMap = schedules.findAllById(
                        scheduleItems.findEnrolledByOfferingId(offeringId).stream()
                                .map(ScheduleItem::getScheduleId).toList()).stream()
                .collect(Collectors.toMap(StudentSchedule::getId, Function.identity()));
        Map<Long, Student> studentMap = students.findAllById(
                        scheduleMap.values().stream().map(StudentSchedule::getStudentId).toList()).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Map<Long, Grade> gradeMap = grades.findByOfferingId(offering.getId()).stream()
                .collect(Collectors.toMap(Grade::getStudentId, Function.identity()));
        return scheduleMap.values().stream()
                .sorted((left, right) -> left.getStudentId().compareTo(right.getStudentId()))
                .map(schedule -> {
                    Student student = studentMap.get(schedule.getStudentId());
                    Grade grade = gradeMap.get(student.getId());
                    return new GradingController.GradeRosterItemView(
                            student.getId(), student.getStudentNumber(), student.getName(),
                            grade == null ? null : grade.getGradeValue());
                }).toList();
    }

    @Transactional
    public GradingController.GradeRosterItemView submitGrade(GradingController.GradeRequest request) {
        Long professorId = currentUser.requireLinkedPersonId();
        CourseOffering offering = requireOwnedOffering(request.offeringId(), professorId);
        if (offering.getStatus() != OfferingStatus.CLOSED) {
            throw new IllegalStateException("只能为已经结束的教学班录入成绩");
        }
        Student student = students.findById(request.studentId())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        boolean enrolled = roster(offering.getId()).stream()
                .anyMatch(item -> item.studentId().equals(student.getId()));
        if (!enrolled) {
            throw new IllegalStateException("该学生不在当前教学班名单中");
        }
        String raw = request.gradeValue() == null ? "" : request.gradeValue().trim();
        if (raw.isBlank()) {
            Grade existing = grades.findByStudentIdAndOfferingId(student.getId(), offering.getId())
                    .orElse(null);
            return new GradingController.GradeRosterItemView(
                    student.getId(), student.getStudentNumber(), student.getName(),
                    existing == null ? null : existing.getGradeValue());
        }
        String normalized = raw.toUpperCase();
        if (!VALID_GRADES.contains(normalized)) {
            throw new IllegalArgumentException("成绩只能是 A、B、C、D、F 或 I");
        }
        Grade grade = grades.findByStudentIdAndOfferingId(student.getId(), offering.getId())
                .orElseGet(() -> new Grade(student.getId(), offering.getId(), offering.getCourseId(),
                        professorId, offering.getSemester(), normalized));
        grade.setGradeValue(normalized);
        grades.save(grade);
        return new GradingController.GradeRosterItemView(
                student.getId(), student.getStudentNumber(), student.getName(), normalized);
    }

    public GradingController.ReportCardView myReportCard(String semester) {
        Long studentId = currentUser.requireLinkedPersonId();
        Student student = students.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("学生档案不存在"));
        Map<Long, Course> courseMap = courses.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        List<Grade> selected = grades.findByStudentIdOrderBySemesterDesc(studentId).stream()
                .filter(grade -> semester == null || semester.isBlank() || semester.equals(grade.getSemester()))
                .filter(grade -> grade.getGradeValue() != null && !grade.getGradeValue().isBlank())
                .toList();
        List<GradingController.ReportCardItemView> views = selected.stream().map(grade -> {
            Course course = courseMap.get(grade.getCourseId());
            return new GradingController.ReportCardItemView(
                    grade.getSemester(), course.getCode(), course.getName(),
                    course.getCredits(), grade.getGradeValue());
        }).toList();
        int attemptedCredits = views.stream().mapToInt(GradingController.ReportCardItemView::credits).sum();
        int earnedCredits = views.stream()
                .filter(item -> List.of("A", "B", "C", "D").contains(item.gradeValue()))
                .mapToInt(GradingController.ReportCardItemView::credits).sum();
        int gpaCredits = views.stream().filter(item -> GRADE_POINTS.containsKey(item.gradeValue()))
                .mapToInt(GradingController.ReportCardItemView::credits).sum();
        int weightedPoints = views.stream().filter(item -> GRADE_POINTS.containsKey(item.gradeValue()))
                .mapToInt(item -> item.credits() * GRADE_POINTS.get(item.gradeValue())).sum();
        BigDecimal gpa = gpaCredits == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(weightedPoints)
                .divide(BigDecimal.valueOf(gpaCredits), 2, RoundingMode.HALF_UP);
        String displayedSemester = semester == null || semester.isBlank() ? "ALL" : semester;
        return new GradingController.ReportCardView(
                student.getStudentNumber(), student.getName(), displayedSemester,
                attemptedCredits, earnedCredits, gpa, views);
    }

    private CourseOffering requireOwnedOffering(Long offeringId, Long professorId) {
        CourseOffering offering = offerings.findById(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("教学班不存在"));
        if (!professorId.equals(offering.getProfessorId())) {
            throw new IllegalStateException("只能维护自己任教的课程");
        }
        return offering;
    }
}
