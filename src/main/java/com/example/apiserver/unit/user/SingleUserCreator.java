package com.example.apiserver.unit.user;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.User;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SingleUserCreator implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public SingleUserCreator(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<User> createUser(final String nfcCardId,
                                 final String name,
                                 final boolean isStudent,
                                 final boolean isTeacher,
                                 final String teacherId,
                                 final String createdBy) {
        return Mono.fromSupplier(() -> {
            final var id = Utils.generateUserDID();
            final var user = service.createUser(id, nfcCardId, name, isStudent, isTeacher, teacherId, createdBy);
            return user;
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
