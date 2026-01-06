package com.example.apiserver.service.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class User {
    public abstract String getId();

    public abstract String getNfcCardId();

    public abstract String getName();

    public abstract boolean isStudent();

    public abstract boolean isTeacher();

    public abstract String getTeacherId();

    public abstract int getCreationSource();

    public abstract String getCreatedBy();

    public abstract long getCreatedAt();

    public abstract int getUpdateSource();

    public abstract String getUpdatedBy();

    public abstract long getUpdatedAt();

    public abstract boolean isDeleted();

    public static User of(final String id,
                          final String nfcCardId,
                          final String name,
                          final boolean isStudent,
                          final boolean isTeacher,
                          final String teacherId,
                          final int creationSource,
                          final String createdBy,
                          final long createdAt,
                          final int updateSource,
                          final String updatedBy,
                          final long updatedAt,
                          final boolean isDeleted) {
        return ImmutableUser.builder()
                .id(id)
                .nfcCardId(nfcCardId)
                .name(name)
                .isStudent(isStudent)
                .isTeacher(isTeacher)
                .teacherId(teacherId)
                .creationSource(creationSource)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updateSource(updateSource)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .isDeleted(isDeleted)
                .build();
    }

    @Value.Immutable
    public static abstract class UserCreation {
        public abstract String getId();

        public abstract int getNfcCardId();

        public abstract String getName();

        public abstract boolean isStudent();

        public abstract boolean isTeacher();

        public abstract String getTeacherId();

    }
}
