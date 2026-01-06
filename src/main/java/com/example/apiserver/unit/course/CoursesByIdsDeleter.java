package com.example.apiserver.unit.course;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import reactor.core.publisher.Mono;

import java.util.List;

public class CoursesByIdsDeleter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public CoursesByIdsDeleter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Void> delete(final List<String> ids,
                             final String deleter) {
        return Mono.fromRunnable(() -> service.deleteCourses(ids, deleter));
    }
}
