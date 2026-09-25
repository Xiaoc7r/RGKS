package com.jlu.registration.module.operations;

import java.math.BigDecimal;
import java.time.Instant;
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

    @GetMapping("/overview")
    public OperationsOverview overview(@RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.overview(semester);
    }

    @PostMapping("/close-registration")
    public CloseSummary closeRegistration(@RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.closeRegistration(semester);
    }

    @GetMapping("/billing")
    public List<BillingView> billing(@RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.listBilling(semester);
    }

    /** success=false 可在答辩时演示外部计费系统暂时不可用。 */
    @PostMapping("/billing/{billingId}/attempt")
    public BillingView attemptBilling(@PathVariable Long billingId,
                                      @RequestParam(defaultValue = "true") boolean success) {
        return service.attemptBilling(billingId, success);
    }

    @PostMapping("/billing/{billingId}/mark-sent")
    public BillingView markSent(@PathVariable Long billingId) {
        return service.attemptBilling(billingId, true);
    }

    public record OperationsOverview(String semester, boolean registrationOpen,
                                     long offeringCount, long submittedScheduleCount,
                                     long billingCount, BigDecimal billedAmount) {
    }

    public record CloseSummary(String semester, int offeringCount,
                               int cancelledOfferingCount, int promotedAlternateCount,
                               int finalizedScheduleCount, int billingCount,
                               BigDecimal totalBillingAmount) {
    }

    public record BillingView(Long id, Long studentId, String studentNumber,
                              String studentName, String semester, BigDecimal amount,
                              BillingStatus status, Integer retryCount, Instant lastAttemptAt) {
    }
}
