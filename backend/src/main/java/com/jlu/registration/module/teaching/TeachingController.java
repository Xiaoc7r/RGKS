package com.jlu.registration.module.teaching;

import java.util.List;

import com.jlu.registration.module.catalog.OfferingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teaching")
public class TeachingController {

    private final TeachingService service;

    public TeachingController(TeachingService service) {
        this.service = service;
    }

    @GetMapping("/my-offerings")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<TeachingOfferingView> myOfferings(
            @RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.myOfferings(semester);
    }

    @PostMapping("/offerings/{offeringId}/select")
    @PreAuthorize("hasRole('PROFESSOR')")
    public TeachingOfferingView selectOffering(@PathVariable Long offeringId) {
        return service.selectOffering(offeringId);
    }

    @GetMapping("/offerings/{offeringId}/roster")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<RosterItemView> roster(@PathVariable Long offeringId) {
        return service.roster(offeringId);
    }

    @PutMapping("/grades")
    @PreAuthorize("hasRole('PROFESSOR')")
    public RosterItemView submitGrade(@Valid @RequestBody GradeRequest request) {
        return service.submitGrade(request);
    }

    @GetMapping("/my-report-card")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ReportCardItemView> myReportCard() {
        return service.myReportCard();
    }

    public record TeachingOfferingView(
            Long offeringId,
            String courseCode,
            String courseName,
            Integer dayOfWeek,
            Integer startPeriod,
            Integer endPeriod,
            long enrolled,
            OfferingStatus status
    ) {
    }

    public record RosterItemView(Long studentId, String studentNumber, String studentName,
                                 String gradeValue) {
    }

    public record GradeRequest(@NotNull Long offeringId, @NotNull Long studentId,
                               @NotBlank String gradeValue) {
    }

    public record ReportCardItemView(String semester, String courseCode,
                                     String courseName, String gradeValue) {
    }
}
