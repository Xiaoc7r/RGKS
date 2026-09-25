package com.jlu.registration.module.people;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
@PreAuthorize("hasRole('REGISTRAR')")
public class PeopleController {

    private final PeopleService service;

    public PeopleController(PeopleService service) {
        this.service = service;
    }

    @GetMapping("/students")
    public List<StudentView> students() {
        return service.listStudents();
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentView createStudent(@Valid @RequestBody StudentRequest request) {
        return service.createStudent(request);
    }

    @PutMapping("/students/{id}")
    public StudentView updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return service.updateStudent(id, request);
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id) {
        service.deleteStudent(id);
    }

    @GetMapping("/professors")
    public List<ProfessorView> professors() {
        return service.listProfessors();
    }

    @PostMapping("/professors")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessorView createProfessor(@Valid @RequestBody ProfessorRequest request) {
        return service.createProfessor(request);
    }

    @PutMapping("/professors/{id}")
    public ProfessorView updateProfessor(@PathVariable Long id, @Valid @RequestBody ProfessorRequest request) {
        return service.updateProfessor(id, request);
    }

    @DeleteMapping("/professors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfessor(@PathVariable Long id) {
        service.deleteProfessor(id);
    }

    public record StudentRequest(
            @NotBlank String studentNumber,
            @NotBlank String name,
            @NotNull LocalDate dateOfBirth,
            String identityNumber,
            @NotNull PersonStatus status,
            @NotBlank String major,
            LocalDate graduationDate
    ) {
    }

    public record ProfessorRequest(
            @NotBlank String employeeNumber,
            @NotBlank String name,
            @NotNull LocalDate dateOfBirth,
            String identityNumber,
            @NotNull PersonStatus status,
            @NotBlank String department
    ) {
    }

    /** 身份证件只返回掩码，完整号码仅在管理员新增或主动修改时写入。 */
    public record StudentView(
            Long id,
            String studentNumber,
            String name,
            LocalDate dateOfBirth,
            String maskedIdentityNumber,
            PersonStatus status,
            String major,
            LocalDate graduationDate
    ) {
    }

    public record ProfessorView(
            Long id,
            String employeeNumber,
            String name,
            LocalDate dateOfBirth,
            String maskedIdentityNumber,
            PersonStatus status,
            String department
    ) {
    }
}
