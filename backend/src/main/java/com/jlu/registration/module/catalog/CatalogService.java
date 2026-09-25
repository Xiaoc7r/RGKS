package com.jlu.registration.module.catalog;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jlu.registration.module.people.Professor;
import com.jlu.registration.module.people.ProfessorRepository;
import com.jlu.registration.module.registration.ScheduleItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CourseRepository courses;
    private final CourseOfferingRepository offerings;
    private final ProfessorRepository professors;
    private final ScheduleItemRepository scheduleItems;

    public CatalogService(CourseRepository courses,
                          CourseOfferingRepository offerings,
                          ProfessorRepository professors,
                          ScheduleItemRepository scheduleItems) {
        this.courses = courses;
        this.offerings = offerings;
        this.professors = professors;
        this.scheduleItems = scheduleItems;
    }

    public List<CatalogController.OfferingView> listOfferings(String semester) {
        Map<Long, Course> courseMap = courses.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, Professor> professorMap = professors.findAll().stream()
                .collect(Collectors.toMap(Professor::getId, Function.identity()));
        return offerings.findBySemesterOrderByDayOfWeekAscStartPeriodAsc(semester).stream()
                .map(offering -> {
                    Course course = courseMap.get(offering.getCourseId());
                    Professor professor = professorMap.get(offering.getProfessorId());
                    long enrolled = scheduleItems.countEnrolledByOfferingId(offering.getId());
                    Course prerequisite = course.getPrerequisiteCourseId() == null
                            ? null : courseMap.get(course.getPrerequisiteCourseId());
                    return new CatalogController.OfferingView(
                            offering.getId(), course.getId(), course.getCode(), course.getName(),
                            course.getDepartment(), course.getCredits(),
                            prerequisite == null ? null : prerequisite.getCode(), course.getTuition(),
                            professor == null ? "待定" : professor.getName(),
                            offering.getSemester(), offering.getDayOfWeek(),
                            offering.getStartPeriod(), offering.getEndPeriod(),
                            offering.getCapacity(), enrolled,
                            Math.max(0, offering.getCapacity() - enrolled), offering.getStatus());
                })
                .toList();
    }

    public List<CatalogController.CourseView> listCourses() {
        Map<Long, Course> courseMap = courses.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return courseMap.values().stream()
                .sorted((left, right) -> left.getCode().compareTo(right.getCode()))
                .map(course -> {
                    Course prerequisite = course.getPrerequisiteCourseId() == null
                            ? null : courseMap.get(course.getPrerequisiteCourseId());
                    return new CatalogController.CourseView(
                            course.getId(), course.getCode(), course.getName(), course.getDepartment(),
                            course.getCredits(), prerequisite == null ? null : prerequisite.getCode(),
                            course.getTuition());
                }).toList();
    }

    public CatalogController.CatalogStatusView status() {
        return new CatalogController.CatalogStatusView(
                "LEGACY_CATALOG_OPEN_SQL_SIMULATOR", true, 10, Instant.now());
    }
}
