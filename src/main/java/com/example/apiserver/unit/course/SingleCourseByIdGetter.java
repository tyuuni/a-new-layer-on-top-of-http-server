package com.example.apiserver.unit.course;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.Course;
import com.example.apiserver.service.model.User;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

public class SingleCourseByIdGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public SingleCourseByIdGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Optional<Course>> getCourseById(final String id) {
        return Mono.fromSupplier(() -> {
            final var course = service.getCourseById(id);
            return Optional.ofNullable(course);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
