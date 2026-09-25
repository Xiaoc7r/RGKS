package com.jlu.registration.module.teaching;

import java.util.List;

import com.jlu.registration.module.catalog.OfferingStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/eligible-offerings")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<TeachingOfferingView> eligibleOfferings(
            @RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.eligibleOfferings(semester);
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

    @DeleteMapping("/offerings/{offeringId}/selection")
    @PreAuthorize("hasRole('PROFESSOR')")
    public void deselectOffering(@PathVariable Long offeringId) {
        service.deselectOffering(offeringId);
    }

    @GetMapping("/offerings/{offeringId}/roster")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<RosterItemView> roster(@PathVariable Long offeringId) {
        return service.roster(offeringId);
    }

    public record TeachingOfferingView(
            Long offeringId,
            String courseCode,
            String courseName,
            String department,
            Integer dayOfWeek,
            Integer startPeriod,
            Integer endPeriod,
            long enrolled,
            OfferingStatus status,
            boolean ownedByMe
    ) {
    }

    public record RosterItemView(Long studentId, String studentNumber, String studentName) {
    }
}
