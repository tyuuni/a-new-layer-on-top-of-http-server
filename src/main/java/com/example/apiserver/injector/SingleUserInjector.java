package com.example.apiserver.injector;

import com.example.apiserver.core.ExampleResponse;
import com.example.apiserver.core.HttpParameter;
import com.example.apiserver.core.InjectedResource;
import com.example.apiserver.core.ResourceInjector;
import com.example.apiserver.ErrorCodes;
import com.example.apiserver.core.exception.CodeMessageErrorResponse;
import com.example.apiserver.core.exception.NonexistingResource;
import com.example.apiserver.service.model.User;
import com.example.apiserver.unit.user.SingleUserByIdGetter;
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
public class SingleUserInjector implements ResourceInjector<SingleUserInjector.InjectedUser> {

    private final SingleUserByIdGetter singleUserByIdGetter;
    private final String path;

    private static final String INVALID_USER_ID = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.INVALID_USER_ID.value(),
                    "invalid user id")
    );

    private static final List<ExampleResponse> FAILURE_RESPONSES = ImmutableList.of(
            ExampleResponse.of(HttpCode.NOT_FOUND, INVALID_USER_ID)
    );

    public SingleUserInjector(final SingleUserByIdGetter singleUserByIdGetter,
                              final String path) {
        this.singleUserByIdGetter = singleUserByIdGetter;
        this.path = path;
    }

    private static final String INJECTION_KEY = "eg-user";

    @Override
    public SingleUserInjector.InjectedUser extract(final Context context) {
        return context.attribute(INJECTION_KEY);
    }

    @Override
    public boolean inject(final Context context) {
        final var userId = context.pathParam(path);
        final var userOptional = singleUserByIdGetter.getUserById(userId).block();
        if (userOptional.isEmpty()) {
            throw new NonexistingResource(String.format("Invalid user id: %s", userId), INVALID_USER_ID);
        }
        context.attribute(INJECTION_KEY, SingleUserInjector.InjectedUser.from(userOptional.get()));
        return true;
    }

    @Override
    public List<HttpParameter> getParameters() {
        return ImmutableList.of(HttpParameter.pathVariable(path));
    }
    @Override
    public List<ExampleResponse> getFailureResponses() {
        return FAILURE_RESPONSES;
    }

    @Override
    public Class<SingleUserInjector.InjectedUser> getResourceClass() {
        return SingleUserInjector.InjectedUser.class;
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableInjectedUser.class)
    @JsonDeserialize(as = ImmutableInjectedUser.class)
    public static abstract class InjectedUser implements InjectedResource {
        public abstract String getId();

        public abstract String getName();

        public abstract String getNfcCardId();

        public abstract boolean isStudent();

        public abstract boolean isTeacher();

        public abstract String getTeacherId();

        static SingleUserInjector.InjectedUser from(final User user) {
            return ImmutableInjectedUser.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .nfcCardId(user.getNfcCardId())
                    .isStudent(user.isStudent())
                    .isTeacher(user.isTeacher())
                    .teacherId(user.getTeacherId())
                    .build();
        }
    }
}
