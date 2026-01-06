package com.example.apiserver.apis;

import com.example.apiserver.apis.model.JointEntityResponse;
import com.example.apiserver.core.ApiBuilder;
import com.example.apiserver.core.ApiContext;
import com.example.apiserver.injector.SingleCourseInjector;
import com.example.apiserver.core.reqres.ResponseMapperFactory;
import com.example.apiserver.unit.user.SingleUserByIdGetter;
import com.example.apiserver.core.reqres.RequestValidatorFactory;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

public class AllCourseApis {

    @Value.Immutable
    @JsonSerialize(as = ImmutableSingleCourseResponsePublic.class)
    @JsonDeserialize(as = ImmutableSingleCourseResponsePublic.class)
    static abstract class SingleCourseResponsePublic {
        abstract String getId();

        abstract String getName();

        @Nullable
        abstract JointEntityResponse getTeacher();
    }

    static void getSingleCourse(final ApiBuilder apiBuilder) {
        apiBuilder.get("/course/{courseId}", "get single course")
                .requiresResourceInjection(SingleCourseInjector.InjectedCourse.class, "{courseId}")
                .requiresBusinessUnits(SingleUserByIdGetter.class)
                .handle(RequestValidatorFactory.cleanUrlAndEmptyBodyValidator(), (singleCourse, singleUserByIdGetter, isValid) -> {
                    return singleUserByIdGetter.getUserById(singleCourse.getTeacherId())
                            .map(userOptional -> {
                               final var teacher = userOptional.get();
                               return ImmutableSingleCourseResponsePublic.builder()
                                       .id(singleCourse.getId())
                                       .name(singleCourse.getName())
                                       .teacher(JointEntityResponse.of(teacher.getId(), teacher.getName(), false))
                                       .build();
                            });
                }, ResponseMapperFactory.jsonResponseMapper200(SingleCourseResponsePublic.class));
    }

    public static void initializeAll(final ApiContext apiContext) {
        getSingleCourse(apiContext.newApiBuilder());
    }
}
