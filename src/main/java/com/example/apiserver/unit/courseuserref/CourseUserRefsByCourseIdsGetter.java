package com.example.apiserver.unit.courseuserref;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.CourseStudentRef;
import reactor.core.publisher.Mono;

import java.util.List;

public class CourseUserRefsByCourseIdsGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public CourseUserRefsByCourseIdsGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<List<CourseStudentRef>> getByCourseIds(final List<String> courseIds) {
        return Mono.fromCallable(() -> service.getCourseStudentRefsByCourseIds(courseIds));
    }
}
