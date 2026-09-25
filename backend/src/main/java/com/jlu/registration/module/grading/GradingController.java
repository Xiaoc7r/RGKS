package com.jlu.registration.module.grading;

import java.math.BigDecimal;
import java.util.List;

import com.jlu.registration.module.catalog.OfferingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grading")
public class GradingController {

    private final GradingService service;

    public GradingController(GradingService service) {
        this.service = service;
    }

    @GetMapping("/my-offerings")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<GradingOfferingView> myOfferings(
            @RequestParam(defaultValue = "2026-SPRING") String semester) {
        return service.myOfferings(semester);
    }

    @GetMapping("/offerings/{offeringId}/roster")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<GradeRosterItemView> roster(@PathVariable Long offeringId) {
        return service.roster(offeringId);
    }

    @PutMapping("/grades")
    @PreAuthorize("hasRole('PROFESSOR')")
    public GradeRosterItemView submitGrade(@Valid @RequestBody GradeRequest request) {
        return service.submitGrade(request);
    }

    @GetMapping("/my-report-card")
    @PreAuthorize("hasRole('STUDENT')")
    public ReportCardView myReportCard(
            @RequestParam(required = false) String semester) {
        return service.myReportCard(semester);
    }

    public record GradingOfferingView(
            Long offeringId,
            String semester,
            String courseCode,
            String courseName,
            long enrolled,
            OfferingStatus status
    ) {
    }

    public record GradeRosterItemView(
            Long studentId,
            String studentNumber,
            String studentName,
            String gradeValue
    ) {
    }

    public record GradeRequest(
            @NotNull Long offeringId,
            @NotNull Long studentId,
            String gradeValue
    ) {
    }

    public record ReportCardItemView(
            String semester,
            String courseCode,
            String courseName,
            Integer credits,
            String gradeValue
    ) {
    }

    public record ReportCardView(
            String studentNumber,
            String studentName,
            String semester,
            int attemptedCredits,
            int earnedCredits,
            BigDecimal gradePointAverage,
            List<ReportCardItemView> items
    ) {
    }
}
