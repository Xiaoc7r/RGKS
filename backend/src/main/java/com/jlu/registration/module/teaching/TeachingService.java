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
import com.jlu.registration.module.people.PersonStatus;
import com.jlu.registration.module.people.Professor;
import com.jlu.registration.module.people.ProfessorRepository;
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

    private final CourseOfferingRepository offerings;
    private final CourseRepository courses;
    private final RegistrationWindowRepository windows;
    private final ScheduleItemRepository scheduleItems;
    private final StudentScheduleRepository schedules;
    private final StudentRepository students;
    private final ProfessorRepository professors;
    private final CurrentUserService currentUser;

    public TeachingService(CourseOfferingRepository offerings,
                           CourseRepository courses,
                           RegistrationWindowRepository windows,
                           ScheduleItemRepository scheduleItems,
                           StudentScheduleRepository schedules,
                           StudentRepository students,
                           ProfessorRepository professors,
                           CurrentUserService currentUser) {
        this.offerings = offerings;
        this.courses = courses;
        this.windows = windows;
        this.scheduleItems = scheduleItems;
        this.schedules = schedules;
        this.students = students;
        this.professors = professors;
        this.currentUser = currentUser;
    }

    public List<TeachingController.TeachingOfferingView> eligibleOfferings(String semester) {
        Professor professor = currentProfessor();
        Map<Long, Course> courseMap = courseMap();
        return offerings.findBySemesterOrderByDayOfWeekAscStartPeriodAsc(semester).stream()
                .filter(offering -> offering.getStatus() == OfferingStatus.OPEN)
                .filter(offering -> offering.getProfessorId() == null
                        || professor.getId().equals(offering.getProfessorId()))
                .filter(offering -> {
                    Course course = courseMap.get(offering.getCourseId());
                    return course != null && professor.getDepartment().equals(course.getDepartment());
                })
                .map(offering -> view(offering, courseMap.get(offering.getCourseId()), professor.getId()))
                .toList();
    }

    public List<TeachingController.TeachingOfferingView> myOfferings(String semester) {
        Long professorId = currentProfessor().getId();
        Map<Long, Course> courseMap = courseMap();
        return offerings.findByProfessorIdAndSemesterOrderByDayOfWeekAscStartPeriodAsc(
                        professorId, semester).stream()
                .map(offering -> view(offering, courseMap.get(offering.getCourseId()), professorId))
                .toList();
    }

    @Transactional
    public TeachingController.TeachingOfferingView selectOffering(Long offeringId) {
        Professor professor = currentProfessor();
        CourseOffering target = offerings.findByIdForUpdate(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("教学班不存在"));
        requireOpenWindow(target);
        if (target.getProfessorId() != null && !target.getProfessorId().equals(professor.getId())) {
            throw new IllegalStateException("该教学班已有其他教师任教");
        }
        Course course = courses.findById(target.getCourseId())
                .orElseThrow(() -> new IllegalStateException("课程目录数据缺失"));
        if (!professor.getDepartment().equals(course.getDepartment())) {
            throw new IllegalStateException("只能选择本院系开设的课程");
        }
        boolean conflict = offerings.findByProfessorIdAndSemesterOrderByDayOfWeekAscStartPeriodAsc(
                        professor.getId(), target.getSemester()).stream()
                .filter(existing -> !existing.getId().equals(target.getId()))
                .anyMatch(target::conflictsWith);
        if (conflict) {
            throw new IllegalStateException("该教学班与已有任课安排冲突");
        }
        target.setProfessorId(professor.getId());
        return view(target, course, professor.getId());
    }

    @Transactional
    public void deselectOffering(Long offeringId) {
        Professor professor = currentProfessor();
        CourseOffering target = offerings.findByIdForUpdate(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("教学班不存在"));
        requireOpenWindow(target);
        if (!professor.getId().equals(target.getProfessorId())) {
            throw new IllegalStateException("只能退选自己任教的教学班");
        }
        target.setProfessorId(null);
    }

    public List<TeachingController.RosterItemView> roster(Long offeringId) {
        CourseOffering offering = requireOwnedOffering(offeringId, currentProfessor().getId());
        List<ScheduleItem> enrolledItems = scheduleItems.findEnrolledByOfferingId(offering.getId());
        Map<Long, StudentSchedule> scheduleMap = schedules.findAllById(
                        enrolledItems.stream().map(ScheduleItem::getScheduleId).toList()).stream()
                .collect(Collectors.toMap(StudentSchedule::getId, Function.identity()));
        Map<Long, Student> studentMap = students.findAllById(
                        scheduleMap.values().stream().map(StudentSchedule::getStudentId).toList()).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        return scheduleMap.values().stream()
                .map(StudentSchedule::getStudentId)
                .distinct()
                .sorted()
                .map(studentMap::get)
                .map(student -> new TeachingController.RosterItemView(
                        student.getId(), student.getStudentNumber(), student.getName()))
                .toList();
    }

    private Professor currentProfessor() {
        Long professorId = currentUser.requireLinkedPersonId();
        Professor professor = professors.findById(professorId)
                .orElseThrow(() -> new IllegalStateException("教师档案不存在"));
        if (professor.getStatus() != PersonStatus.ACTIVE) {
            throw new IllegalStateException("只有在职教师可以选择任课课程");
        }
        return professor;
    }

    private void requireOpenWindow(CourseOffering offering) {
        RegistrationWindow window = windows.findBySemester(offering.getSemester())
                .orElseThrow(() -> new IllegalStateException("当前学期未配置选课窗口"));
        if (!window.isOpen() || offering.getStatus() != OfferingStatus.OPEN) {
            throw new IllegalStateException("选课已经关闭，教师不能调整任课课程");
        }
    }

    private CourseOffering requireOwnedOffering(Long offeringId, Long professorId) {
        CourseOffering offering = offerings.findById(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("教学班不存在"));
        if (!professorId.equals(offering.getProfessorId())) {
            throw new IllegalStateException("只能查看自己任教的课程");
        }
        return offering;
    }

    private Map<Long, Course> courseMap() {
        return courses.findAll().stream().collect(Collectors.toMap(Course::getId, Function.identity()));
    }

    private TeachingController.TeachingOfferingView view(CourseOffering offering,
                                                           Course course,
                                                           Long professorId) {
        if (course == null) {
            throw new IllegalStateException("课程目录数据缺失");
        }
        return new TeachingController.TeachingOfferingView(
                offering.getId(), course.getCode(), course.getName(), course.getDepartment(),
                offering.getDayOfWeek(), offering.getStartPeriod(), offering.getEndPeriod(),
                scheduleItems.countEnrolledByOfferingId(offering.getId()), offering.getStatus(),
                professorId.equals(offering.getProfessorId()));
    }
}
