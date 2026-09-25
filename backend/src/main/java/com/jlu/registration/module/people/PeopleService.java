package com.jlu.registration.module.people;

import java.util.List;

import com.jlu.registration.module.identity.UserAccountRepository;
import com.jlu.registration.module.identity.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PeopleService {

    private final StudentRepository students;
    private final ProfessorRepository professors;
    private final UserAccountRepository accounts;

    public PeopleService(StudentRepository students, ProfessorRepository professors,
                         UserAccountRepository accounts) {
        this.students = students;
        this.professors = professors;
        this.accounts = accounts;
    }

    public List<PeopleController.StudentView> listStudents() {
        return students.findAll().stream().map(this::studentView).toList();
    }

    public List<PeopleController.ProfessorView> listProfessors() {
        return professors.findAll().stream().map(this::professorView).toList();
    }

    @Transactional
    public PeopleController.StudentView createStudent(PeopleController.StudentRequest request) {
        if (students.existsByStudentNumber(request.studentNumber())) {
            throw new IllegalArgumentException("学号已存在");
        }
        requireIdentityNumber(request.identityNumber());
        return studentView(students.save(new Student(
                request.studentNumber(), request.name(), request.dateOfBirth(),
                request.identityNumber(), request.status(), request.major(), request.graduationDate())));
    }

    @Transactional
    public PeopleController.StudentView updateStudent(Long id, PeopleController.StudentRequest request) {
        Student student = students.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        if (students.existsByStudentNumberAndIdNot(request.studentNumber(), id)) {
            throw new IllegalArgumentException("学号已存在");
        }
        student.setStudentNumber(request.studentNumber());
        student.setName(request.name());
        student.setDateOfBirth(request.dateOfBirth());
        if (request.identityNumber() != null && !request.identityNumber().isBlank()) {
            student.setIdentityNumber(request.identityNumber().trim());
        }
        student.setStatus(request.status());
        student.setMajor(request.major());
        student.setGraduationDate(request.graduationDate());
        return studentView(students.save(student));
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (!students.existsById(id)) {
            throw new IllegalArgumentException("学生不存在");
        }
        if (accounts.existsByLinkedPersonIdAndRole(id, UserRole.STUDENT)) {
            throw new IllegalStateException("学生仍有关联登录账号，不能删除");
        }
        students.deleteById(id);
    }

    @Transactional
    public PeopleController.ProfessorView createProfessor(PeopleController.ProfessorRequest request) {
        if (professors.existsByEmployeeNumber(request.employeeNumber())) {
            throw new IllegalArgumentException("工号已存在");
        }
        requireIdentityNumber(request.identityNumber());
        return professorView(professors.save(new Professor(
                request.employeeNumber(), request.name(), request.dateOfBirth(),
                request.identityNumber(), request.status(), request.department())));
    }

    @Transactional
    public PeopleController.ProfessorView updateProfessor(Long id, PeopleController.ProfessorRequest request) {
        Professor professor = professors.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        if (professors.existsByEmployeeNumberAndIdNot(request.employeeNumber(), id)) {
            throw new IllegalArgumentException("工号已存在");
        }
        professor.setEmployeeNumber(request.employeeNumber());
        professor.setName(request.name());
        professor.setDateOfBirth(request.dateOfBirth());
        if (request.identityNumber() != null && !request.identityNumber().isBlank()) {
            professor.setIdentityNumber(request.identityNumber().trim());
        }
        professor.setStatus(request.status());
        professor.setDepartment(request.department());
        return professorView(professors.save(professor));
    }

    @Transactional
    public void deleteProfessor(Long id) {
        if (!professors.existsById(id)) {
            throw new IllegalArgumentException("教师不存在");
        }
        if (accounts.existsByLinkedPersonIdAndRole(id, UserRole.PROFESSOR)) {
            throw new IllegalStateException("教师仍有关联登录账号，不能删除");
        }
        professors.deleteById(id);
    }

    private void requireIdentityNumber(String identityNumber) {
        if (identityNumber == null || identityNumber.isBlank()) {
            throw new IllegalArgumentException("新增档案时必须填写身份证件号");
        }
    }

    private PeopleController.StudentView studentView(Student student) {
        return new PeopleController.StudentView(
                student.getId(), student.getStudentNumber(), student.getName(),
                student.getDateOfBirth(), mask(student.getIdentityNumber()), student.getStatus(),
                student.getMajor(), student.getGraduationDate());
    }

    private PeopleController.ProfessorView professorView(Professor professor) {
        return new PeopleController.ProfessorView(
                professor.getId(), professor.getEmployeeNumber(), professor.getName(),
                professor.getDateOfBirth(), mask(professor.getIdentityNumber()), professor.getStatus(),
                professor.getDepartment());
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "未填写";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }
}
