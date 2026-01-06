package com.example.apiserver.unit.user;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.model.User;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

public class SingleUserByNfcCardIdGetter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public SingleUserByNfcCardIdGetter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Optional<User>> getUserByNfcCardId(final String nfcCardId) {
        return Mono.fromSupplier(() -> {
            final var user = service.getUserByNfcCardId(nfcCardId);
            return Optional.ofNullable(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
