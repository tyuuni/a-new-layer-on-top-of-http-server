package com.example.apiserver.unit.courseuserref;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.CourseStudentRef;
import kotlin.Pair;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class CourseUserRefsByIdsDeleter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public CourseUserRefsByIdsDeleter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Void> deleteRefsByIds(final List<String> courseUserRefIds,
                                      final String deletedBy) {
        return Mono.fromRunnable(() -> service.deleteCourseStudentRefs(courseUserRefIds, deletedBy));
    }
}
