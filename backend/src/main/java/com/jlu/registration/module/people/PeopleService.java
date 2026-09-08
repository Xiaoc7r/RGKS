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

    public List<Student> listStudents() {
        return students.findAll();
    }

    public List<Professor> listProfessors() {
        return professors.findAll();
    }

    @Transactional
    public Student createStudent(PeopleController.StudentRequest request) {
        return students.save(new Student(
                request.studentNumber(), request.name(), request.major(), request.graduationDate()));
    }

    @Transactional
    public Student updateStudent(Long id, PeopleController.StudentRequest request) {
        Student student = students.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        student.setStudentNumber(request.studentNumber());
        student.setName(request.name());
        student.setMajor(request.major());
        student.setGraduationDate(request.graduationDate());
        return students.save(student);
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
    public Professor createProfessor(PeopleController.ProfessorRequest request) {
        return professors.save(new Professor(
                request.employeeNumber(), request.name(), request.department()));
    }

    @Transactional
    public Professor updateProfessor(Long id, PeopleController.ProfessorRequest request) {
        Professor professor = professors.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        professor.setEmployeeNumber(request.employeeNumber());
        professor.setName(request.name());
        professor.setDepartment(request.department());
        return professors.save(professor);
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
}
