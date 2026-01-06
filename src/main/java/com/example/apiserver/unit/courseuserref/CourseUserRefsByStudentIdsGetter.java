package com.example.apiserver.unit.courseuserref;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.CourseStudentRef;
import reactor.core.publisher.Mono;

import java.util.List;

public class CourseUserRefsByStudentIdsGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public CourseUserRefsByStudentIdsGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<List<CourseStudentRef>> getByStudentIds(final List<String> studentIds) {
        return Mono.fromCallable(() -> service.getCourseStudentRefsByStudentIds(studentIds));
    }
}
