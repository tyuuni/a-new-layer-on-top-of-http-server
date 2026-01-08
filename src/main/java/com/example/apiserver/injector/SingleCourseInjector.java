package com.example.apiserver.injector;

import com.example.apiserver.core.ExampleResponse;
import com.example.apiserver.core.HttpParameter;
import com.example.apiserver.core.InjectedResource;
import com.example.apiserver.core.ResourceInjector;
import com.example.apiserver.ErrorCodes;
import com.example.apiserver.core.exception.CodeMessageErrorResponse;
import com.example.apiserver.core.exception.NonexistingResource;
import com.example.apiserver.service.model.Course;
import com.example.apiserver.unit.course.SingleCourseByIdGetter;
import com.example.apiserver.util.JSONUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.immutables.value.Value;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

@ThreadSafe
public class SingleCourseInjector implements ResourceInjector<SingleCourseInjector.InjectedCourse> {

    private final SingleCourseByIdGetter singleCourseByIdGetter;
    private final String path;

    private static final String INVALID_COURSE_ID = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.INVALID_COURSE_ID.value(),
                    "invalid course id")
    );

    private static final List<ExampleResponse> FAILURE_RESPONSES = ImmutableList.of(
            ExampleResponse.of(HttpCode.NOT_FOUND, INVALID_COURSE_ID));

    public SingleCourseInjector(final SingleCourseByIdGetter singleCourseByIdGetter,
                                final String path) {
        this.singleCourseByIdGetter = singleCourseByIdGetter;
        this.path = path;
    }

    private static final String INJECTION_KEY = "eg-course";

    @Override
    public SingleCourseInjector.InjectedCourse extract(final Context context) {
        return context.attribute(INJECTION_KEY);
    }

    @Override
    public boolean inject(final Context context) {
        final var courseId = context.pathParam(path);
        final var courseOptional = singleCourseByIdGetter.getCourseById(courseId).block();
        if (courseOptional.isEmpty()) {
            throw new NonexistingResource(String.format("Invalid course id: %s", courseId), INVALID_COURSE_ID);
        }
        context.attribute(INJECTION_KEY, SingleCourseInjector.InjectedCourse.from(courseOptional.get()));
        return true;
    }

    @Override
    public List<HttpParameter> getParameters() {
        return ImmutableList.of(
                HttpParameter.pathVariable(path));
    }

    @Override
    public List<ExampleResponse> getFailureResponses() {
        return FAILURE_RESPONSES;
    }

    @Override
    public Class<SingleCourseInjector.InjectedCourse> getResourceClass() {
        return SingleCourseInjector.InjectedCourse.class;
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableInjectedCourse.class)
    @JsonDeserialize(as = ImmutableInjectedCourse.class)
    public static abstract class InjectedCourse implements InjectedResource {
        public abstract String getId();

        public abstract String getName();

        public abstract String getTeacherId();
        static SingleCourseInjector.InjectedCourse from(final Course course) {
            return ImmutableInjectedCourse.builder()
                    .id(course.getId())
                    .name(course.getName())
                    .teacherId(course.getTeacherId())
                    .build();
        }
    }
}
