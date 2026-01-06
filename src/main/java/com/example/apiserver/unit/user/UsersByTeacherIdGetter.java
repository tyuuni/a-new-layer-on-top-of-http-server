package com.example.apiserver.unit.user;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.User;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class UsersByTeacherIdGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public UsersByTeacherIdGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<List<User>> getUsersByTeacherId(final String teacherId) {
        return Mono.fromSupplier(() -> service.getUsersByTeacherId(teacherId)).subscribeOn(Schedulers.boundedElastic());
    }
}
