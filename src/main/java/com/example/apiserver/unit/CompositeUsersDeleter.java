package com.example.apiserver.unit;

import com.example.apiserver.core.BusinessUnit;
import com.example.apiserver.service.model.CourseStudentRef;
import com.example.apiserver.unit.courseuserref.CourseUserRefsByIdsDeleter;
import com.example.apiserver.unit.courseuserref.CourseUserRefsByStudentIdsGetter;
import com.example.apiserver.unit.user.UsersByIdsDeleter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeUsersDeleter implements BusinessUnit {
    private final UsersByIdsDeleter usersByIdsDeleter;
    private final CourseUserRefsByStudentIdsGetter courseUserRefsByStudentIdsGetter;
    private final CourseUserRefsByIdsDeleter courseUserRefsByIdsDeleter;

    public CompositeUsersDeleter(final UsersByIdsDeleter usersByIdsDeleter,
                                 final CourseUserRefsByStudentIdsGetter courseUserRefsByStudentIdsGetter,
                                 final CourseUserRefsByIdsDeleter courseUserRefsByIdsDeleter) {
        this.usersByIdsDeleter = usersByIdsDeleter;
        this.courseUserRefsByStudentIdsGetter = courseUserRefsByStudentIdsGetter;
        this.courseUserRefsByIdsDeleter = courseUserRefsByIdsDeleter;
    }

    public Mono<Void> deleteUser(final String userId,
                                 final String deletedBy) {
        return usersByIdsDeleter.delete(List.of(userId), deletedBy)
                .then(courseUserRefsByStudentIdsGetter.getByStudentIds(List.of(userId)))
                .flatMap(courseUserRefs -> courseUserRefsByIdsDeleter.deleteRefsByIds(courseUserRefs.stream()
                        .map(CourseStudentRef::getId)
                        .collect(Collectors.toList()), userId));
    }
}
