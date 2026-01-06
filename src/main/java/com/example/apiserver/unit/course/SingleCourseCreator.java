package com.example.apiserver.unit.course;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.Course;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SingleCourseCreator implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public SingleCourseCreator(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Course> createCourse(final String name,
                                     final String teacherId,
                                     final String createdBy) {
        return Mono.fromSupplier(() -> {
            final String id = Utils.generateCourseDID();
            final var createdCourse = service.createCourse(id, name, teacherId, createdBy);
            return createdCourse;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
