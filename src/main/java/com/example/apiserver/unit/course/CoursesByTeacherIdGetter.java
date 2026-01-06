package com.example.apiserver.unit.course;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.Course;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class CoursesByTeacherIdGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public CoursesByTeacherIdGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<List<Course>> getCoursesByTeacherId(final String teacherId) {
        return Mono.fromSupplier(() -> service.getCoursesByTeacherId(teacherId)).subscribeOn(Schedulers.boundedElastic());
    }
}
