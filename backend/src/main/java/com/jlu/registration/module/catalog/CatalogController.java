package com.jlu.registration.module.catalog;

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

    public record OfferingView(
            Long offeringId,
            String courseCode,
            String courseName,
            Integer credits,
            String professorName,
            String semester,
            Integer dayOfWeek,
            Integer startPeriod,
            Integer endPeriod,
            Integer capacity,
            long enrolled,
            OfferingStatus status
    ) {
    }
}
