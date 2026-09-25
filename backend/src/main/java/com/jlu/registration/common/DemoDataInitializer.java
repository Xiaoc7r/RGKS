package com.jlu.registration.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.jlu.registration.module.catalog.Course;
import com.jlu.registration.module.catalog.CourseOffering;
import com.jlu.registration.module.catalog.CourseOfferingRepository;
import com.jlu.registration.module.catalog.CourseRepository;
import com.jlu.registration.module.catalog.OfferingStatus;
import com.jlu.registration.module.grading.Grade;
import com.jlu.registration.module.grading.GradeRepository;
import com.jlu.registration.module.identity.UserAccount;
import com.jlu.registration.module.identity.UserAccountRepository;
import com.jlu.registration.module.identity.UserRole;
import com.jlu.registration.module.people.PersonStatus;
import com.jlu.registration.module.people.Professor;
import com.jlu.registration.module.people.ProfessorRepository;
import com.jlu.registration.module.people.Student;
import com.jlu.registration.module.people.StudentRepository;
import com.jlu.registration.module.registration.ChoiceType;
import com.jlu.registration.module.registration.EnrollmentStatus;
import com.jlu.registration.module.registration.RegistrationWindow;
import com.jlu.registration.module.registration.RegistrationWindowRepository;
import com.jlu.registration.module.registration.ScheduleItem;
import com.jlu.registration.module.registration.ScheduleItemRepository;
import com.jlu.registration.module.registration.ScheduleStatus;
import com.jlu.registration.module.registration.StudentSchedule;
import com.jlu.registration.module.registration.StudentScheduleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * 初始化一组可直接答辩演示的数据。所有创建动作都先查后写，重复启动不会制造重复记录。
 */
@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner demoData(DemoDataSeeder seeder) {
        return args -> seeder.seed();
    }

    @Configuration
    static class DemoDataSeeder {

        private static final String DEPARTMENT = "计算机科学与技术学院";

        private final StudentRepository students;
        private final ProfessorRepository professors;
        private final UserAccountRepository accounts;
        private final CourseRepository courses;
        private final CourseOfferingRepository offerings;
        private final RegistrationWindowRepository windows;
        private final StudentScheduleRepository schedules;
        private final ScheduleItemRepository scheduleItems;
        private final GradeRepository grades;
        private final PasswordEncoder passwordEncoder;

        DemoDataSeeder(StudentRepository students,
                       ProfessorRepository professors,
                       UserAccountRepository accounts,
                       CourseRepository courses,
                       CourseOfferingRepository offerings,
                       RegistrationWindowRepository windows,
                       StudentScheduleRepository schedules,
                       ScheduleItemRepository scheduleItems,
                       GradeRepository grades,
                       PasswordEncoder passwordEncoder) {
            this.students = students;
            this.professors = professors;
            this.accounts = accounts;
            this.courses = courses;
            this.offerings = offerings;
            this.windows = windows;
            this.schedules = schedules;
            this.scheduleItems = scheduleItems;
            this.grades = grades;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public void seed() {
            Student student = student("20260001", "张同学", "110101200601010011");
            Student backgroundOne = student("20260002", "陈同学", "110101200602020022");
            Student backgroundTwo = student("20260003", "赵同学", "110101200603030033");
            Professor professor = professor("T001", "李老师", "110101198001010044");
            Professor secondProfessor = professor("T002", "王老师", "110101198202020055");

            Course foundation = course("CS100", "程序设计基础", 3, null, "1200.00");
            Course java = course("CS201", "Java程序设计", 3, foundation.getId(), "1500.00");
            Course software = course("SE201", "软件工程", 3, null, "1500.00");
            Course database = course("DB201", "数据库系统", 3, null, "1500.00");
            Course web = course("WEB201", "Web应用开发", 3, null, "1500.00");
            Course network = course("NET201", "计算机网络", 3, null, "1500.00");
            Course testing = course("SE301", "软件测试", 2, null, "1200.00");

            List<CourseOffering> fall = offerings
                    .findBySemesterOrderByDayOfWeekAscStartPeriodAsc("2026-FALL");
            if (fall.isEmpty()) {
                fall = offerings.saveAll(List.of(
                        offering(java, professor, 1, 1, 2),
                        offering(software, professor, 2, 1, 2),
                        offering(database, secondProfessor, 3, 1, 2),
                        offering(web, secondProfessor, 4, 1, 2),
                        offering(network, null, 5, 1, 2),
                        offering(testing, null, 1, 3, 4)
                ));
            }

            // 两名背景学生保证四个主选教学班达到最低开课人数，便于演示结算。
            List<CourseOffering> assignedPrimary = List.of(
                    offeringForCourse(fall, java), offeringForCourse(fall, software),
                    offeringForCourse(fall, database), offeringForCourse(fall, web));
            seedSubmittedSchedule(backgroundOne, assignedPrimary);
            seedSubmittedSchedule(backgroundTwo, assignedPrimary);

            CourseOffering historical = offerings
                    .findBySemesterOrderByDayOfWeekAscStartPeriodAsc("2026-SPRING")
                    .stream().findFirst().orElseGet(() -> {
                        CourseOffering value = new CourseOffering(
                                foundation.getId(), "2026-SPRING", professor.getId(), 1, 1, 2);
                        value.setStatus(OfferingStatus.CLOSED);
                        return offerings.save(value);
                    });
            seedHistoricalEnrollment(student, historical);
            if (grades.findByStudentIdAndOfferingId(student.getId(), historical.getId()).isEmpty()) {
                grades.save(new Grade(student.getId(), historical.getId(), foundation.getId(),
                        professor.getId(), "2026-SPRING", "A"));
            }
            if (windows.findBySemester("2026-FALL").isEmpty()) {
                windows.save(new RegistrationWindow("2026-FALL"));
            }
            createAccount("student1", "Student123!", student.getName(),
                    UserRole.STUDENT, student.getId());
            createAccount("professor1", "Professor123!", professor.getName(),
                    UserRole.PROFESSOR, professor.getId());
            createAccount("registrar", "Registrar123!", "注册管理员",
                    UserRole.REGISTRAR, null);
        }

        private Student student(String number, String name, String identityNumber) {
            Student value = students.findByStudentNumber(number).orElseGet(() -> students.save(
                    new Student(number, name, LocalDate.of(2006, 1, 1), identityNumber,
                            PersonStatus.ACTIVE, "软件工程", LocalDate.of(2030, 6, 30))));
            if (value.getStatus() == null) {
                value.setStatus(PersonStatus.ACTIVE);
            }
            return value;
        }

        private Professor professor(String number, String name, String identityNumber) {
            Professor value = professors.findByEmployeeNumber(number).orElseGet(() -> professors.save(
                    new Professor(number, name, LocalDate.of(1980, 1, 1), identityNumber,
                            PersonStatus.ACTIVE, DEPARTMENT)));
            if (value.getStatus() == null) {
                value.setStatus(PersonStatus.ACTIVE);
            }
            return value;
        }

        private Course course(String code, String name, int credits,
                              Long prerequisiteCourseId, String tuition) {
            Course value = courses.findByCode(code).orElseGet(() -> courses.save(
                    new Course(code, name, DEPARTMENT, credits,
                            prerequisiteCourseId, new BigDecimal(tuition))));
            if (value.getDepartment() == null) {
                value.setDepartment(DEPARTMENT);
            }
            return value;
        }

        private CourseOffering offering(Course course, Professor professor,
                                        int day, int start, int end) {
            return new CourseOffering(course.getId(), "2026-FALL",
                    professor == null ? null : professor.getId(), day, start, end);
        }

        private CourseOffering offeringForCourse(List<CourseOffering> values, Course course) {
            return values.stream().filter(value -> value.getCourseId().equals(course.getId()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("演示教学班数据缺失"));
        }

        private void seedSubmittedSchedule(Student student, List<CourseOffering> primaryOfferings) {
            StudentSchedule schedule = schedules.findByStudentIdAndSemester(student.getId(), "2026-FALL")
                    .orElseGet(() -> schedules.save(new StudentSchedule(student.getId(), "2026-FALL")));
            schedule.setStatus(ScheduleStatus.SUBMITTED);
            for (int i = 0; i < primaryOfferings.size(); i++) {
                CourseOffering offering = primaryOfferings.get(i);
                if (!scheduleItems.existsByScheduleIdAndOfferingId(schedule.getId(), offering.getId())) {
                    ScheduleItem item = new ScheduleItem(
                            schedule.getId(), offering.getId(), ChoiceType.PRIMARY, i + 1);
                    item.setStatus(EnrollmentStatus.ENROLLED);
                    scheduleItems.save(item);
                }
            }
        }

        private void seedHistoricalEnrollment(Student student, CourseOffering offering) {
            StudentSchedule schedule = schedules
                    .findByStudentIdAndSemester(student.getId(), offering.getSemester())
                    .orElseGet(() -> schedules.save(
                            new StudentSchedule(student.getId(), offering.getSemester())));
            schedule.setStatus(ScheduleStatus.FINALIZED);
            if (!scheduleItems.existsByScheduleIdAndOfferingId(schedule.getId(), offering.getId())) {
                ScheduleItem item = new ScheduleItem(
                        schedule.getId(), offering.getId(), ChoiceType.PRIMARY, 1);
                item.setStatus(EnrollmentStatus.ENROLLED);
                scheduleItems.save(item);
            }
        }

        private void createAccount(String username, String rawPassword, String displayName,
                                   UserRole role, Long linkedPersonId) {
            if (!accounts.existsByUsername(username)) {
                accounts.save(new UserAccount(username, passwordEncoder.encode(rawPassword),
                        displayName, role, linkedPersonId));
            }
        }
    }
}
