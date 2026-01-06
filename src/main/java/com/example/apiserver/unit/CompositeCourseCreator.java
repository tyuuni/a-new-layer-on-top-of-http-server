package com.example.apiserver.unit;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.model.Course;
import com.example.apiserver.unit.course.SingleCourseCreator;
import com.example.apiserver.unit.courseuserref.CourseUserRefsCreator;
import kotlin.Pair;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeCourseCreator implements BusinessUnit {

    private final SingleCourseCreator singleCourseCreator;
    private final CourseUserRefsCreator courseUserRefsCreator;

    public CompositeCourseCreator(final SingleCourseCreator singleCourseCreator,
                                  final CourseUserRefsCreator courseUserRefsCreator) {
        this.singleCourseCreator = singleCourseCreator;
        this.courseUserRefsCreator = courseUserRefsCreator;
    }

    public Mono<Course> createCourse(final String name,
                                     final String teacherId,
                                     final List<String> studentIds,
                                     final String createdBy) {
        return singleCourseCreator.createCourse(name, teacherId, createdBy)
                .flatMap(course -> {
                    return courseUserRefsCreator.create(studentIds.stream()
                            .map(studentId -> new Pair<>(course.getId(), studentId))
                            .collect(Collectors.toList()), createdBy).thenReturn(course);
                });
    }


}
