package com.jlu.registration.module.catalog;

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
                    return new CatalogController.OfferingView(
                            offering.getId(), course.getCode(), course.getName(), course.getCredits(),
                            professor == null ? "待定" : professor.getName(),
                            offering.getSemester(), offering.getDayOfWeek(),
                            offering.getStartPeriod(), offering.getEndPeriod(),
                            offering.getCapacity(), enrolled, offering.getStatus());
                })
                .toList();
    }
}

