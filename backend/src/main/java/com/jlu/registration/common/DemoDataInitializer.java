package com.jlu.registration.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.jlu.registration.module.catalog.Course;
import com.jlu.registration.module.catalog.CourseOffering;
import com.jlu.registration.module.catalog.CourseOfferingRepository;
import com.jlu.registration.module.catalog.CourseRepository;
import com.jlu.registration.module.catalog.OfferingStatus;
import com.jlu.registration.module.identity.UserAccount;
import com.jlu.registration.module.identity.UserAccountRepository;
import com.jlu.registration.module.identity.UserRole;
import com.jlu.registration.module.people.Professor;
import com.jlu.registration.module.people.ProfessorRepository;
import com.jlu.registration.module.people.Student;
import com.jlu.registration.module.people.StudentRepository;
import com.jlu.registration.module.registration.RegistrationWindow;
import com.jlu.registration.module.registration.RegistrationWindowRepository;
import com.jlu.registration.module.teaching.Grade;
import com.jlu.registration.module.teaching.GradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner demoData(DemoDataSeeder seeder) {
        return args -> seeder.seed();
    }

    @Configuration
    static class DemoDataSeeder {

        private final StudentRepository students;
        private final ProfessorRepository professors;
        private final UserAccountRepository accounts;
        private final CourseRepository courses;
        private final CourseOfferingRepository offerings;
        private final RegistrationWindowRepository windows;
        private final GradeRepository grades;
        private final PasswordEncoder passwordEncoder;

        DemoDataSeeder(StudentRepository students,
                       ProfessorRepository professors,
                       UserAccountRepository accounts,
                       CourseRepository courses,
                       CourseOfferingRepository offerings,
                       RegistrationWindowRepository windows,
                       GradeRepository grades,
                       PasswordEncoder passwordEncoder) {
            this.students = students;
            this.professors = professors;
            this.accounts = accounts;
            this.courses = courses;
            this.offerings = offerings;
            this.windows = windows;
            this.grades = grades;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public void seed() {
            Student student = students.findByStudentNumber("20260001")
                    .orElseGet(() -> students.save(new Student(
                            "20260001", "张同学", "软件工程", LocalDate.of(2030, 6, 30))));
            Professor professor = professors.findByEmployeeNumber("T001")
                    .orElseGet(() -> professors.save(new Professor(
                            "T001", "李老师", "计算机科学与技术学院")));
            Professor secondProfessor = professors.findByEmployeeNumber("T002")
                    .orElseGet(() -> professors.save(new Professor(
                            "T002", "王老师", "计算机科学与技术学院")));

            Course foundation = course("CS100", "程序设计基础", 3, null, "1200.00");
            Course java = course("CS201", "Java程序设计", 3, foundation.getId(), "1500.00");
            Course software = course("SE201", "软件工程", 3, null, "1500.00");
            Course database = course("DB201", "数据库系统", 3, null, "1500.00");
            Course web = course("WEB201", "Web应用开发", 3, null, "1500.00");
            Course network = course("NET201", "计算机网络", 3, null, "1500.00");
            Course testing = course("SE301", "软件测试", 2, null, "1200.00");

            if (offerings.findBySemesterOrderByDayOfWeekAscStartPeriodAsc("2026-FALL").isEmpty()) {
                offerings.saveAll(List.of(
                        offering(java, professor, 1, 1, 2),
                        offering(software, professor, 2, 1, 2),
                        offering(database, secondProfessor, 3, 1, 2),
                        offering(web, secondProfessor, 4, 1, 2),
                        offering(network, professor, 5, 1, 2),
                        offering(testing, secondProfessor, 1, 3, 4)
                ));
            }

            CourseOffering historical = offerings.findBySemesterOrderByDayOfWeekAscStartPeriodAsc("2026-SPRING")
                    .stream().findFirst().orElseGet(() -> {
                        CourseOffering value = new CourseOffering(
                                foundation.getId(), "2026-SPRING", professor.getId(), 1, 1, 2);
                        value.setStatus(OfferingStatus.CLOSED);
                        return offerings.save(value);
                    });
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

        private Course course(String code, String name, int credits,
                              Long prerequisiteCourseId, String tuition) {
            return courses.findByCode(code).orElseGet(() -> courses.save(
                    new Course(code, name, credits, prerequisiteCourseId, new BigDecimal(tuition))));
        }

        private CourseOffering offering(Course course, Professor professor,
                                        int day, int start, int end) {
            return new CourseOffering(course.getId(), "2026-FALL", professor.getId(), day, start, end);
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
