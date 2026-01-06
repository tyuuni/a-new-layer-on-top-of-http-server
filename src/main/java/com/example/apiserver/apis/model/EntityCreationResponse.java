package com.example.apiserver.apis.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityCreationResponse.class)
public abstract class EntityCreationResponse {
    public abstract String getId();

    public static EntityCreationResponse of(final String id) {
        return ImmutableEntityCreationResponse.builder()
                .id(id)
                .build();
    }
}
