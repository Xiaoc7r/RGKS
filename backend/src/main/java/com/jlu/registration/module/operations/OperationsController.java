package com.jlu.registration.module.operations;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations")
@PreAuthorize("hasRole('REGISTRAR')")
public class OperationsController {

    private final OperationsService service;

    public OperationsController(OperationsService service) {
        this.service = service;
    }

    @PostMapping("/close-registration")
    public CloseSummary closeRegistration(
            @RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.closeRegistration(semester);
    }

    @GetMapping("/billing")
    public List<BillingRecord> billing(
            @RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.listBilling(semester);
    }

    @PostMapping("/billing/{billingId}/mark-sent")
    public BillingRecord markSent(@PathVariable Long billingId) {
        return service.markBillingSent(billingId);
    }

    public record CloseSummary(
            String semester,
            int offeringCount,
            int cancelledOfferingCount,
            int promotedAlternateCount,
            int finalizedScheduleCount
    ) {
    }
}
