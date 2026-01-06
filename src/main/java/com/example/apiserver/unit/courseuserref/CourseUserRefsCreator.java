package com.example.apiserver.unit.courseuserref;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.CourseStudentRef;
import kotlin.Pair;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class CourseUserRefsCreator implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public CourseUserRefsCreator(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Void> create(final List<Pair<String, String>> courseUserIdPairs,
                             final String createdBy) {
        return Mono.fromRunnable(() -> service.createCourseStudentRefs(courseUserIdPairs.stream()
                .map(pair -> CourseStudentRef.CourseStudentRefCreation.of(
                        Utils.generateCourseUserRefId(pair.getSecond(), pair.getFirst()),
                        pair.getSecond(),
                        pair.getFirst()))
                .collect(Collectors.toList()), createdBy));
    }
}
