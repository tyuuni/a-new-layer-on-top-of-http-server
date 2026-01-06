package com.example.apiserver.service.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Course {
    public abstract String getId();

    public abstract String getName();

    public abstract String getTeacherId();

    public abstract int getCreationSource();

    public abstract String getCreatedBy();

    public abstract long getCreatedAt();

    public abstract int getUpdateSource();

    public abstract String getUpdatedBy();

    public abstract long getUpdatedAt();

    public abstract boolean isDeleted();

    public static Course of(final String id,
                            final String name,
                            final String teacherId,
                            final int creationSource,
                            final String createdBy,
                            final long createdAt,
                            final int updateSource,
                            final String updatedBy,
                            final long updatedAt,
                            final boolean isDeleted) {
        return ImmutableCourse.builder()
                .id(id)
                .name(name)
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
    public static abstract class CourseCreation {
        public abstract String getId();

        public abstract String getName();

        public abstract String getTeacherId();
    }
}
