package com.example.apiserver.apis;

import com.example.apiserver.core.exception.CodeMessageErrorResponse;
import com.example.apiserver.apis.model.JointEntityResponse;
import com.example.apiserver.core.ApiBuilder;
import com.example.apiserver.core.ApiContext;
import com.example.apiserver.ErrorCodes;
import com.example.apiserver.core.exception.ConflictingRequest;
import com.example.apiserver.core.exception.RequestValidationFailure;
import com.example.apiserver.core.exception.UnauthorizedRequest;
import com.example.apiserver.injector.ApiBasedSemaphore;
import com.example.apiserver.injector.Authenticator;
import com.example.apiserver.injector.SingleUserInjector;
import com.example.apiserver.core.reqres.ResponseMapperFactory;
import com.example.apiserver.service.model.User;
import com.example.apiserver.store.TokenStore;
import com.example.apiserver.unit.user.*;
import com.example.apiserver.util.JSONUtil;
import com.example.apiserver.core.reqres.RequestValidatorFactory;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class AllUserApis {

    @Value.Immutable
    @JsonSerialize(as = ImmutableLoginRequest.class)
    @JsonDeserialize(as = ImmutableLoginRequest.class)
    static abstract class LoginRequest {
        abstract String getName();
        abstract String getNfcCardId();
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableLoginResponse.class)
    @JsonDeserialize(as = ImmutableLoginResponse.class)
    static abstract class LoginResponse {
        abstract boolean getIsSuccessful();
        abstract String getId();
        abstract String getToken();
    }

    static final String LOGIN_FAILURE_INCORRECT_NFC_CARD_ID = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.LOGIN_FAILURE_INCORRECT_USER.value(),
                    "incorrect nfc card id"));

    static void login(final ApiBuilder apiBuilder) {
        apiBuilder.post("/login", "user login. this api requires only nfc card id to match user name")
                .requiresResourceInjection()
                .requiresBusinessUnits(SingleUserByNfcCardIdGetter.class, TokenStore.class)
                .handle(RequestValidatorFactory.buildCleanJsonValidator(LoginRequest.class), (singleUserByNfcCardIdGetter, tokenStore, loginRequest) -> {
                    return singleUserByNfcCardIdGetter.getUserByNfcCardId(loginRequest.getNfcCardId())
                            .handle((userOptional, sink) -> {
                                if (userOptional.isEmpty()) {
                                    sink.error(new RequestValidationFailure(
                                            "incorrect nfc card id",
                                            LOGIN_FAILURE_INCORRECT_NFC_CARD_ID));
                                    return;
                                }
                                final User user = userOptional.get();
                                if (!user.getName().equals(loginRequest.getName())) {
                                    sink.error(new RequestValidationFailure(
                                            "incorrect nfc card id",
                                            LOGIN_FAILURE_INCORRECT_NFC_CARD_ID));
                                    return;
                                }
                                final String token = tokenStore.putToken(user.getId());
                                sink.next(ImmutableLoginResponse.builder()
                                        .isSuccessful(true)
                                        .id(user.getId())
                                        .token(token)
                                        .build());
                            });
                }, ResponseMapperFactory.jsonResponseMapper200(LoginResponse.class))
                .examplesFor406(LOGIN_FAILURE_INCORRECT_NFC_CARD_ID);
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableSingleUserResponsePrivate.class)
    @JsonDeserialize(as = ImmutableSingleUserResponsePrivate.class)
    static abstract class SingleUserResponsePrivate {
        abstract String getId();

        abstract String getNfcCardId();

        abstract String getName();

        abstract boolean isStudent();

        abstract boolean isTeacher();

        @Nullable
        abstract JointEntityResponse getTeacher();
    }

    static void getSingleUserPrivate(final ApiBuilder apiBuilder) {
        apiBuilder.get("/user/{userId}", "get single user by id(requires authentication).")
                .requiresResourceInjection(Authenticator.LoggedInUser.class)
                .requiresResourceInjection(SingleUserInjector.InjectedUser.class, "{userId}")
                .requiresBusinessUnits(SingleUserByIdGetter.class)
                .handle(RequestValidatorFactory.cleanUrlAndEmptyBodyValidator(), (loggedInUser, injectedUser, singleUserByIdGetter, aBoolean) -> {
                    return singleUserByIdGetter.getUserById(injectedUser.getTeacherId())
                            .map(userOptional -> {
                                final JointEntityResponse teacher;
                                if (userOptional.isEmpty()) {
                                    teacher = null;
                                } else {
                                    final User teacherUser = userOptional.get();
                                    teacher = JointEntityResponse.of(
                                            teacherUser.getId(),
                                            teacherUser.getName(),
                                            teacherUser.isDeleted());
                                }
                                return ImmutableSingleUserResponsePrivate.builder()
                                        .id(injectedUser.getId())
                                        .nfcCardId(injectedUser.getNfcCardId())
                                        .name(injectedUser.getName())
                                        .isStudent(injectedUser.isStudent())
                                        .isTeacher(injectedUser.isTeacher())
                                        .teacher(teacher)
                                        .build();
                            });
                },  ResponseMapperFactory.jsonResponseMapper200(SingleUserResponsePrivate.class));
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableStudentsOfTeacherResponse.class)
    @JsonDeserialize(as = ImmutableStudentsOfTeacherResponse.class)
    static abstract class StudentsOfTeacherResponse {
        abstract JointEntityResponse getTeacher();

        abstract int getCount();

        abstract List<SingleUserResponsePrivate> getStudents();
    }

    static void getStudentsOfTeacher(final ApiBuilder apiBuilder) {
        apiBuilder.get("/teacher/{teacherId}/students", "get students of teacher(requires authentication).")
                .requiresResourceInjection(Authenticator.LoggedInUser.class)
                .requiresResourceInjection(SingleUserInjector.InjectedUser.class, "{teacherId}")
                .requiresBusinessUnits(UsersByTeacherIdGetter.class)
                .handle(RequestValidatorFactory.cleanUrlAndEmptyBodyValidator(), (loggedInUser, injectedUser, usersByTeacherIdGetter, aBoolean) -> {
                    if (!loggedInUser.isTeacher()) {
                        return Mono.error(new IllegalArgumentException("only teacher can get students of teacher"));
                    }
                    return usersByTeacherIdGetter.getUsersByTeacherId(injectedUser.getTeacherId())
                            .map(students -> {
                                return ImmutableStudentsOfTeacherResponse.builder()
                                        .teacher(JointEntityResponse.of(
                                                injectedUser.getId(),
                                                injectedUser.getName(),
                                                false))
                                        .count(students.size())
                                        .students(students.stream()
                                                .map(student -> {
                                                    return ImmutableSingleUserResponsePrivate.builder()
                                                            .id(student.getId())
                                                            .nfcCardId(student.getNfcCardId())
                                                            .name(student.getName())
                                                            .isStudent(true)
                                                            .isTeacher(false)
                                                            .teacher(null)
                                                            .build();
                                                })
                                                .collect(Collectors.toList()))
                                        .build();
                            });
                }, ResponseMapperFactory.jsonResponseMapper200(StudentsOfTeacherResponse.class));
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableSingleUserCreationRequest.class)
    @JsonDeserialize(as = ImmutableSingleUserCreationRequest.class)
    static abstract class SingleUserCreationRequest {
        abstract String getNfcCardId();

        abstract String getName();
    }

    static final String USER_CREATION_FAILED_UNAUTHORIZED = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.UNAUTHORIZED_USER_CREATION.value(),
                    "only teachers can create students"));

    static final String USER_CREATION_FAILED_CONFLICTING_NFC_ID = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.CONFLICTING_NFC_ID.value(),
                    "conflicting nfc card id"));

    static void createStudent(final ApiBuilder apiBuilder) {
        apiBuilder.put("/students", "create a student. this api should be called by a teacher.")
                .requiresResourceInjection(Authenticator.LoggedInUser.class)
                .requiresBusinessUnits(SingleUserCreator.class, SingleUserByNfcCardIdGetter.class)
                .handle(RequestValidatorFactory.buildCleanJsonValidator(SingleUserCreationRequest.class), (loggedInUser, singleUserCreator, singleUserByNfcCardIdGetter, request) -> {
                    if (!loggedInUser.isTeacher()) {
                        return Mono.error(new UnauthorizedRequest(String.format("student %s tries to create a student", loggedInUser.getId()), USER_CREATION_FAILED_UNAUTHORIZED));
                    }
                    return singleUserByNfcCardIdGetter.getUserByNfcCardId(request.getNfcCardId())
                            .handle((userOptional, sink) -> {
                                if (userOptional.isPresent()) {
                                    sink.error(new RequestValidationFailure(String.format("user %s creating user with existing nfc card id %s", loggedInUser.getId(), request.getNfcCardId()), USER_CREATION_FAILED_CONFLICTING_NFC_ID));
                                }
                            }).then(singleUserCreator.createUser(request.getNfcCardId(), request.getName(), true, false, loggedInUser.getId(), loggedInUser.getId())
                                    .map(User::getId));
                }, ResponseMapperFactory.singleEntityIdMapper201())
                .examplesFor403(USER_CREATION_FAILED_UNAUTHORIZED)
                .examplesFor406(USER_CREATION_FAILED_CONFLICTING_NFC_ID);
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableSingleUserUpdateRequest.class)
    @JsonDeserialize(as = ImmutableSingleUserUpdateRequest.class)
    static abstract class SingleUserUpdateRequest {

        @Nullable
        abstract String getNfcCardId();

        @Nullable
        abstract String getName();
    }

    private static final String USER_UPDATE_FAILURE_CONCURRENT_REQUESTS = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.USER_UPDATE_CONCURRENT_REQUESTS.value(),
                    "conflicting nfc card id"));

    static void updateSingleUser(final ApiBuilder apiBuilder) {
        apiBuilder.patch("/user/{userId}", "update single user.")
                .requiresResourceInjection(Authenticator.LoggedInUser.class)
                .requiresResourceInjection(ApiBasedSemaphore.InjectedApiSemaphore.class)
                .requiresResourceInjection(SingleUserInjector.InjectedUser.class, "{userId}")
                .requiresBusinessUnits(SingleUserUpdater.class)
                .handle(RequestValidatorFactory.buildCleanJsonValidator(SingleUserUpdateRequest.class), (loggedInUser, injectedApiSemaphore, injectedUser, singleUserUpdater, request) -> {
                    if (injectedApiSemaphore.getAcquiredValue() > 1) {
                        return Mono.error(new ConflictingRequest("concurrent request to update user", USER_UPDATE_FAILURE_CONCURRENT_REQUESTS));
                    }
                    return singleUserUpdater.updateUser(injectedUser.getId(), request.getNfcCardId(), request.getName(), loggedInUser.getId());
                }, ResponseMapperFactory.noContentMapper204());
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableSingleUserResponsePublic.class)
    @JsonDeserialize(as = ImmutableSingleUserResponsePublic.class)
    static abstract class SingleUserResponsePublic {
        abstract String getId();

        abstract String getName();

        abstract boolean isStudent();

        abstract boolean isTeacher();

        @Nullable
        abstract JointEntityResponse getTeacher();
    }

    static void getSingleUserPublic(final ApiBuilder apiBuilder) {
        apiBuilder.get("public/user/{userId}", "get single user by id(publicly accessible).")
                .requiresResourceInjection(SingleUserInjector.InjectedUser.class, "{userId}")
                .requiresBusinessUnits(SingleUserByIdGetter.class)
                .handle(RequestValidatorFactory.cleanUrlAndEmptyBodyValidator(), (injectedUser, singleUserByIdGetter, aBoolean) -> {
                    return singleUserByIdGetter.getUserById(injectedUser.getTeacherId())
                            .map(userOptional -> {
                                final JointEntityResponse teacher;
                                if (userOptional.isEmpty()) {
                                    teacher = null;
                                } else {
                                    final User teacherUser = userOptional.get();
                                    teacher = JointEntityResponse.of(
                                            teacherUser.getId(),
                                            teacherUser.getName(),
                                            teacherUser.isDeleted());
                                }
                                return ImmutableSingleUserResponsePublic.builder()
                                        .id(injectedUser.getId())
                                        .name(injectedUser.getName())
                                        .isStudent(injectedUser.isStudent())
                                        .isTeacher(injectedUser.isTeacher())
                                        .teacher(teacher)
                                        .build();
                            });
                }, ResponseMapperFactory.jsonResponseMapper200(SingleUserResponsePublic.class))
                .examplesFor406();
    }

    public static void initializeAll(final ApiContext apiContext) {

        getSingleUserPrivate(apiContext.newApiBuilder());
        getStudentsOfTeacher(apiContext.newApiBuilder());
        createStudent(apiContext.newApiBuilder());
        updateSingleUser(apiContext.newApiBuilder());

        login(apiContext.newApiBuilder());
        getSingleUserPublic(apiContext.newApiBuilder());
    }
}
