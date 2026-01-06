package com.example.apiserver.unit.user;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.TemporaryMonolithicService;
import reactor.core.publisher.Mono;

import java.util.List;

public class UsersByIdsDeleter implements BusinessUnit {
    private final TemporaryMonolithicService service;

    public UsersByIdsDeleter(final TemporaryMonolithicService service) {
        this.service = service;
    }

    public Mono<Void> delete(final List<String> ids,
                             final String deleter) {
        return Mono.fromRunnable(() -> service.deleteUsers(ids, deleter));
    }
}
