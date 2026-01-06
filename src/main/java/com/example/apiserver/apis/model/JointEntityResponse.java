package com.example.apiserver.apis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Collections;

@Value.Immutable
@JsonSerialize(as = ImmutableJointEntityResponse.class)
@JsonIgnoreProperties()
public abstract class JointEntityResponse {
    public abstract String getId();

    public abstract String getName();

    public abstract boolean isDeleted();

    public static final JointEntityResponse DUMMY_RESPONSE = ImmutableJointEntityResponse.builder()
            .id("")
            .name("")
            .isDeleted(false)
            .build();

    public static JointEntityResponse of(final String id,
                                         final String name,
                                         final boolean isDeleted) {
        return ImmutableJointEntityResponse.builder()
                .id(id)
                .name(name)
                .isDeleted(isDeleted)
                .build();
    }
}
