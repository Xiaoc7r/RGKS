package com.jlu.registration.module.registration;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
@PreAuthorize("hasRole('STUDENT')")
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    @GetMapping("/my-schedule")
    public ScheduleView mySchedule(@RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.mySchedule(semester);
    }

    @PostMapping("/selections")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleView addSelection(@Valid @RequestBody SelectionRequest request) {
        return service.addSelection(request);
    }

    @DeleteMapping("/selections/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSelection(@PathVariable Long itemId,
                                @RequestParam(defaultValue = "2026-FALL") String semester) {
        service.removeSelection(itemId, semester);
    }

    @PostMapping("/submit")
    public ScheduleView submit(@RequestParam(defaultValue = "2026-FALL") String semester) {
        return service.submit(semester);
    }

    public record SelectionRequest(
            @NotBlank String semester,
            @NotNull Long offeringId,
            @NotNull ChoiceType choiceType,
            @NotNull @Min(1) @Max(4) Integer priority
    ) {
    }

    public record ScheduleView(
            Long id,
            String semester,
            ScheduleStatus status,
            List<ScheduleItemView> items
    ) {
    }

    public record ScheduleItemView(
            Long itemId,
            Long offeringId,
            String courseCode,
            String courseName,
            ChoiceType choiceType,
            Integer priority,
            EnrollmentStatus status,
            Integer dayOfWeek,
            Integer startPeriod,
            Integer endPeriod
    ) {
    }
}
