package com.example.apiserver.unit;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.model.CourseStudentRef;
import com.example.apiserver.unit.course.CoursesByIdsDeleter;
import com.example.apiserver.unit.courseuserref.CourseUserRefsByCourseIdsGetter;
import com.example.apiserver.unit.courseuserref.CourseUserRefsByIdsDeleter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeCourseDeleter implements BusinessUnit {
    private final CoursesByIdsDeleter coursesByIdsDeleter;
    private final CourseUserRefsByCourseIdsGetter courseUserRefsByCourseIdsGetter;
    private final CourseUserRefsByIdsDeleter courseUserRefsByIdsDeleter;

    public CompositeCourseDeleter(final CoursesByIdsDeleter coursesByIdsDeleter,
                                  final CourseUserRefsByCourseIdsGetter courseUserRefsByCourseIdsGetter,
                                  final CourseUserRefsByIdsDeleter courseUserRefsByIdsDeleter) {
        this.coursesByIdsDeleter = coursesByIdsDeleter;
        this.courseUserRefsByCourseIdsGetter = courseUserRefsByCourseIdsGetter;
        this.courseUserRefsByIdsDeleter = courseUserRefsByIdsDeleter;
    }

    public Mono<Void> deleteCourse(final String courseId,
                                   final String deletedBy) {
        return coursesByIdsDeleter.delete(List.of(courseId), deletedBy)
                .then(courseUserRefsByCourseIdsGetter.getByCourseIds(List.of(courseId)))
                .flatMap(courseUserRefs -> courseUserRefsByIdsDeleter.deleteRefsByIds(courseUserRefs.stream()
                        .map(CourseStudentRef::getId)
                        .collect(Collectors.toList()), deletedBy));
    }
}
