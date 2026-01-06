package com.example.apiserver.unit.course;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.Course;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class CoursesByIdsGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public CoursesByIdsGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<List<Course>> getCoursesByIds(final List<String> ids) {
        return Mono.fromSupplier(() -> {
            final var courses = service.getCoursesByIds(ids);
            return courses;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
