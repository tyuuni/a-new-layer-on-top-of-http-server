package com.example.apiserver.unit.user;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.User;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

public class SingleUserByIdGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public SingleUserByIdGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Optional<User>> getUserById(final String id) {
        return Mono.fromSupplier(() -> {
            final var user = service.getUserById(id);
            return Optional.ofNullable(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
