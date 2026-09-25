package com.jlu.registration.module.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/offerings")
    public List<OfferingView> offerings(
            @RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.listOfferings(semester);
    }

    @GetMapping("/courses")
    public List<CourseView> courses() {
        return service.listCourses();
    }

    @GetMapping("/status")
    public CatalogStatusView status() {
        return service.status();
    }

    public record OfferingView(
            Long offeringId,
            Long courseId,
            String courseCode,
            String courseName,
            String department,
            Integer credits,
            String prerequisiteCourseCode,
            BigDecimal tuition,
            String professorName,
            String semester,
            Integer dayOfWeek,
            Integer startPeriod,
            Integer endPeriod,
            Integer capacity,
            long enrolled,
            long remainingSeats,
            OfferingStatus status
    ) {
    }

    public record CourseView(
            Long courseId,
            String courseCode,
            String courseName,
            String department,
            Integer credits,
            String prerequisiteCourseCode,
            BigDecimal tuition
    ) {
    }

    /**
     * 课程设计无法连接原始 Ingres/DEC VAX，本接口明确展示本机只读适配边界。
     */
    public record CatalogStatusView(
            String source,
            boolean readOnly,
            int latencyBudgetSeconds,
            Instant checkedAt
    ) {
    }
}
