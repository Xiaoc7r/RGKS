package com.jlu.registration.module.teaching;

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
import com.jlu.registration.module.registration.RegistrationWindow;
import com.jlu.registration.module.registration.RegistrationWindowRepository;
import com.jlu.registration.module.registration.ScheduleItem;
import com.jlu.registration.module.registration.ScheduleItemRepository;
import com.jlu.registration.module.registration.StudentSchedule;
import com.jlu.registration.module.registration.StudentScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeachingService {

    private static final List<String> VALID_GRADES = List.of("A", "B", "C", "D", "F", "I");

    private final CourseOfferingRepository offerings;
    private final CourseRepository courses;
    private final RegistrationWindowRepository windows;
    private final ScheduleItemRepository scheduleItems;
    private final StudentScheduleRepository schedules;
    private final StudentRepository students;
    private final GradeRepository grades;
    private final CurrentUserService currentUser;

    public TeachingService(CourseOfferingRepository offerings,
                           CourseRepository courses,
                           RegistrationWindowRepository windows,
                           ScheduleItemRepository scheduleItems,
                           StudentScheduleRepository schedules,
                           StudentRepository students,
                           GradeRepository grades,
                           CurrentUserService currentUser) {
        this.offerings = offerings;
        this.courses = courses;
        this.windows = windows;
        this.scheduleItems = scheduleItems;
        this.schedules = schedules;
        this.students = students;
        this.grades = grades;
        this.currentUser = currentUser;
    }

    public List<TeachingController.TeachingOfferingView> myOfferings(String semester) {
        Long professorId = currentUser.requireLinkedPersonId();
        Map<Long, Course> courseMap = courses.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return offerings.findByProfessorIdAndSemesterOrderByDayOfWeekAscStartPeriodAsc(
                        professorId, semester).stream()
                .map(offering -> {
                    Course course = courseMap.get(offering.getCourseId());
                    return new TeachingController.TeachingOfferingView(
                            offering.getId(), course.getCode(), course.getName(),
                            offering.getDayOfWeek(), offering.getStartPeriod(), offering.getEndPeriod(),
                            scheduleItems.countEnrolledByOfferingId(offering.getId()), offering.getStatus());
                }).toList();
    }

    @Transactional
    public TeachingController.TeachingOfferingView selectOffering(Long offeringId) {
        Long professorId = currentUser.requireLinkedPersonId();
        CourseOffering target = offerings.findByIdForUpdate(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("教学班不存在"));
        RegistrationWindow window = windows.findBySemester(target.getSemester())
                .orElseThrow(() -> new IllegalStateException("当前学期未配置选课窗口"));
        if (!window.isOpen() || target.getStatus() != OfferingStatus.OPEN) {
            throw new IllegalStateException("选课已经关闭，教师不能调整任课课程");
        }
        if (target.getProfessorId() != null && !target.getProfessorId().equals(professorId)) {
            throw new IllegalStateException("该教学班已有其他教师任教");
        }
        boolean conflict = offerings.findByProfessorIdAndSemesterOrderByDayOfWeekAscStartPeriodAsc(
                        professorId, target.getSemester()).stream()
                .filter(existing -> !existing.getId().equals(target.getId()))
                .anyMatch(target::conflictsWith);
        if (conflict) {
            throw new IllegalStateException("该教学班与已有任课安排冲突");
        }
        target.setProfessorId(professorId);
        Course course = courses.findById(target.getCourseId())
                .orElseThrow(() -> new IllegalStateException("课程目录数据缺失"));
        return new TeachingController.TeachingOfferingView(
                target.getId(), course.getCode(), course.getName(), target.getDayOfWeek(),
                target.getStartPeriod(), target.getEndPeriod(),
                scheduleItems.countEnrolledByOfferingId(target.getId()), target.getStatus());
    }

    public List<TeachingController.RosterItemView> roster(Long offeringId) {
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
        return scheduleMap.values().stream().map(schedule -> {
            Student student = studentMap.get(schedule.getStudentId());
            Grade grade = gradeMap.get(student.getId());
            return new TeachingController.RosterItemView(
                    student.getId(), student.getStudentNumber(), student.getName(),
                    grade == null ? null : grade.getGradeValue());
        }).toList();
    }

    @Transactional
    public TeachingController.RosterItemView submitGrade(TeachingController.GradeRequest request) {
        Long professorId = currentUser.requireLinkedPersonId();
        CourseOffering offering = requireOwnedOffering(request.offeringId(), professorId);
        String normalized = request.gradeValue().trim().toUpperCase();
        if (!VALID_GRADES.contains(normalized)) {
            throw new IllegalArgumentException("成绩只能是 A、B、C、D、F 或 I");
        }
        Student student = students.findById(request.studentId())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        boolean enrolled = roster(offering.getId()).stream()
                .anyMatch(item -> item.studentId().equals(student.getId()));
        if (!enrolled) {
            throw new IllegalStateException("该学生不在当前教学班名单中");
        }
        Grade grade = grades.findByStudentIdAndOfferingId(student.getId(), offering.getId())
                .orElseGet(() -> new Grade(student.getId(), offering.getId(), offering.getCourseId(),
                        professorId, offering.getSemester(), normalized));
        grade.setGradeValue(normalized);
        grades.save(grade);
        return new TeachingController.RosterItemView(
                student.getId(), student.getStudentNumber(), student.getName(), normalized);
    }

    public List<TeachingController.ReportCardItemView> myReportCard() {
        Long studentId = currentUser.requireLinkedPersonId();
        Map<Long, Course> courseMap = courses.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return grades.findByStudentIdOrderBySemesterDesc(studentId).stream().map(grade -> {
            Course course = courseMap.get(grade.getCourseId());
            return new TeachingController.ReportCardItemView(
                    grade.getSemester(), course.getCode(), course.getName(), grade.getGradeValue());
        }).toList();
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

