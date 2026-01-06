package com.example.apiserver.injector;

import com.example.apiserver.ErrorCodes;
import com.example.apiserver.core.HttpParameter;
import com.example.apiserver.core.InjectedResource;
import com.example.apiserver.core.ResourceInjector;
import com.example.apiserver.core.exception.CodeMessageErrorResponse;
import com.example.apiserver.core.exception.UnauthenticatedRequest;
import com.example.apiserver.service.model.User;
import com.example.apiserver.store.TokenStore;
import com.example.apiserver.unit.user.SingleUserByIdGetter;
import com.example.apiserver.util.JSONUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

@ThreadSafe
public class Authenticator implements ResourceInjector<Authenticator.LoggedInUser> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    private static final String USER_HEADER = "eg-user-id";
    private static final String TOKEN_HEADER = "eg-token";

    private static final List<HttpParameter> PARAMETERS = ImmutableList.of(
            HttpParameter.header(USER_HEADER),
            HttpParameter.header(TOKEN_HEADER));

    private static final String HEADER_MISSING = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.AUTH_HEADER_MISSING.value(),
                    "requires authentication headers"));

    private static final String TOKEN_EXPIRED = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.TOKEN_EXPIRED.value(),
                    "token expired")
    );

    private static final List<String> FAILURE_RESPONSES = ImmutableList.of(HEADER_MISSING, TOKEN_EXPIRED);

    private static final String INJECTION_KEY = "eg-logged-in-user";

    private final SingleUserByIdGetter singleUserByIdGetter;
    private final TokenStore tokenStore;

    public Authenticator(final SingleUserByIdGetter singleUserByIdGetter,
                         final TokenStore tokenStore) {
        this.singleUserByIdGetter = singleUserByIdGetter;
        this.tokenStore = tokenStore;
    }

    @Override
    public LoggedInUser extract(final Context context) {
        return context.attribute(INJECTION_KEY);
    }

    @Override
    public boolean inject(final Context context) {
        final var userId = context.header(USER_HEADER);
        final var token = context.header(TOKEN_HEADER);
        if (userId == null || token == null) {
            throw new UnauthenticatedRequest("Missing authentication headers", HEADER_MISSING);
        }
        final var existingToken = tokenStore.retrieveToken(userId);
        if (existingToken == null || !existingToken.equals(token)) {
            throw new UnauthenticatedRequest("Token expired", TOKEN_EXPIRED);
        }
        final var user = singleUserByIdGetter.getUserById(userId).block();
        if (user.isEmpty()) {
            throw new UnauthenticatedRequest("Token expired", TOKEN_EXPIRED);
        }
        context.attribute(INJECTION_KEY, LoggedInUser.from(user.get()));
        return true;
    }

    @Override
    public List<HttpParameter> getParameters() {
        return PARAMETERS;
    }

    @Override
    public HttpCode getFailureCode() {
        return HttpCode.UNAUTHORIZED;
    }

    @Override
    public List<String> getFailureResponses() {
        return FAILURE_RESPONSES;
    }

    @Override
    public Class<LoggedInUser> getResourceClass() {
        return LoggedInUser.class;
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableLoggedInUser.class)
    @JsonDeserialize(as = ImmutableLoggedInUser.class)
    public static abstract class LoggedInUser implements InjectedResource {
        public abstract String getId();

        public abstract String getName();

        public abstract String getNfcCardId();

        public abstract boolean isStudent();

        public abstract boolean isTeacher();

        public abstract String getTeacherId();

        static LoggedInUser from(final User user) {
            return ImmutableLoggedInUser.builder()
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
