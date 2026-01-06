package com.example.apiserver.service.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class CourseStudentRef {
    public abstract String getId();

    public abstract String getStudentId();

    public abstract String getCourseId();

    public abstract int getCreationSource();

    public abstract String getCreatedBy();

    public abstract long getCreatedAt();

    public abstract int getUpdateSource();

    public abstract String getUpdatedBy();

    public abstract long getUpdatedAt();

    public static CourseStudentRef of(final String id,
                                      final String studentId,
                                      final String courseId,
                                      final int creationSource,
                                      final String createdBy,
                                      final long createdAt,
                                      final int updateSource,
                                      final String updatedBy,
                                      final long updatedAt) {
        return ImmutableCourseStudentRef.builder()
                .id(id)
                .studentId(studentId)
                .courseId(courseId)
                .creationSource(creationSource)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updateSource(updateSource)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }

    @Value.Immutable
    public static abstract class CourseStudentRefCreation {
        public abstract String getId();
        public abstract String getStudentId();
        public abstract String getCourseId();

        public static CourseStudentRefCreation of(final String id,
                                  final String studentId,
                                  final String courseId) {
            return ImmutableCourseStudentRefCreation.builder()
                    .id(id)
                    .studentId(studentId)
                    .courseId(courseId)
                    .build();
        }
    }

}
