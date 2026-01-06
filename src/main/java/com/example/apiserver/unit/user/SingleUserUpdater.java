package com.example.apiserver.unit.user;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

public class SingleUserUpdater implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public SingleUserUpdater(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Void> updateUser(final String id,
                                 @Nullable final String nfcCardId,
                                 @Nullable final String name,
                                 final String updater) {
        return Mono.fromRunnable(() -> service.updateUser(id, nfcCardId, name, updater));
    }
}
