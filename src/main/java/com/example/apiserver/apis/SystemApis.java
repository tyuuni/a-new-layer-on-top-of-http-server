package com.example.apiserver.apis;

import com.example.apiserver.injector.Authenticator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.example.apiserver.core.ApiContext;
import com.example.apiserver.core.ApiDefinition;
import com.example.apiserver.core.ExampleResponse;
import com.example.apiserver.core.reqres.ResponseMapperFactory;
import com.example.apiserver.core.reqres.RequestValidatorFactory;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SystemApis {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    static final Set<Class<?>> BASIC_CLASSES = ImmutableSet.of(
            int.class, long.class, boolean.class,
            Integer.class, Long.class, Boolean.class, String.class
    );

    static String toFieldName(final String name) {
        if (name.startsWith("get")) {
            return String.format("%s%s", Character.toLowerCase(name.charAt(3)), name.substring(4));
        }
        return name;
    }

    @Value.Immutable
    @Value.Style(jdkOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(as = ImmutableParamSpec.class)
    static abstract class ParamSpec {

        abstract boolean isNullable();

        abstract String getName();

        abstract String getType();

        @Nullable
        abstract ModelSpec getModel();

        static ParamSpec from(final Field field) {
            final var builder = ImmutableParamSpec.builder()
                    .name(field.getName())
                    .type(field.getType().getSimpleName())
                    .isNullable(field.getAnnotation(Nullable.class) != null);
            if (BASIC_CLASSES.contains(field.getType())) {
                return builder.build();
            }
            if (List.class.isAssignableFrom(field.getType())) {
                final var innerClass = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                try {
                    final var clazz = Class.forName(innerClass.getTypeName());
                    return builder
                            .model(ModelSpec.from(clazz))
                            .build();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return builder
                    .model(ModelSpec.from(field.getType()))
                    .build();
        }

        static ParamSpec from(final Method method) {
            final var builder = ImmutableParamSpec.builder()
                    .name(toFieldName(method.getName()))
                    .type(method.getReturnType().getSimpleName())
                    .isNullable(method.getAnnotation(Nullable.class) != null);
            if (BASIC_CLASSES.contains(method.getReturnType())) {
                return builder.build();
            }
            if (List.class.isAssignableFrom(method.getReturnType())) {
                final var innerClass = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                try {
                    final var clazz = Class.forName(innerClass.getTypeName());
                    return builder
                            .model(ModelSpec.from(clazz))
                            .build();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return builder
                    .model(ModelSpec.from(method.getReturnType()))
                    .build();
        }
    }

    @Value.Immutable
    @Value.Style(jdkOnly = true)
    @JsonSerialize(as = ImmutableModelSpec.class)
    static abstract class ModelSpec {
        abstract String getName();

        @Nullable
        abstract ModelSpec getInner();

        @Nullable
        abstract List<ParamSpec> getParamsSpec();

        static <T> ModelSpec from(final Class<T> clazz) {
            if (BASIC_CLASSES.contains(clazz)) {
                return ImmutableModelSpec.builder()
                        .name(clazz.getSimpleName())
                        .build();
            }
            if (List.class.isAssignableFrom(clazz)) {
                final var innerClass = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
                try {
                    final var innerClazz = Class.forName(innerClass.getTypeName());
                    return ImmutableModelSpec.builder()
                            .name("List")
                            .inner(ModelSpec.from(innerClazz))
                            .build();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
            final var builder = ImmutableModelSpec.builder()
                    .name(clazz.getSimpleName());
            if (Modifier.isAbstract(clazz.getModifiers())) {
                final var methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> !Modifier.isStatic(method.getModifiers()))
                        .collect(Collectors.toList());
                if (methods.size() > 0) {
                    builder.paramsSpec(methods.stream()
                            .map(ParamSpec::from)
                            .collect(Collectors.toList()));
                }
            } else {
                final var fields = Arrays.stream(clazz.getDeclaredFields())
                        .filter(field -> !Modifier.isStatic(field.getModifiers()))
                        .collect(Collectors.toList());
                if (fields.size() > 0) {
                    builder.paramsSpec(fields.stream()
                            .map(ParamSpec::from)
                            .collect(Collectors.toList()));
                }
            }
            return builder.build();
        }
    }

    @Value.Immutable
    @Value.Style(jdkOnly = true)
    @JsonSerialize(as = ImmutableApiDefinitionResponse.class)
    static abstract class ApiDefinitionResponse {
        abstract String getMethod();

        abstract String getPath();

        abstract String getDescription();

        @Nullable
        abstract String getRequestType();

        @Nullable
        abstract ModelSpec getRequestParams();

        abstract int getResponseStatus();

        @Nullable
        abstract String getResponseType();

        abstract List<ExampleResponse> getGeneralResponses();

        abstract List<ExampleResponse> getCustomResponses();

        @Nullable
        abstract ModelSpec getResponseParams();

        static <R, T> ApiDefinitionResponse from(final ApiDefinition<R, T> apiDefinition) {
            final var builder = ImmutableApiDefinitionResponse.builder()
                    .method(apiDefinition.getType().name())
                    .path(apiDefinition.getPath())
                    .description(apiDefinition.getDescription())
                    .responseStatus(apiDefinition.getResponseMapper().getStatusCode().getStatus())
                    .generalResponses(apiDefinition.getInjectors().stream()
                            .flatMap(injector -> injector.getFailureResponses().stream())
                            .collect(Collectors.toList()))
                    .customResponses(apiDefinition.getCustomResponseExamples());
            if (apiDefinition.getValidator().getContentType() != null) {
                builder.requestType(apiDefinition.getValidator().getContentType().getMimeType());
            }
            if (apiDefinition.getValidator().getModel() != null) {
                builder.requestParams(ModelSpec.from(apiDefinition.getValidator().getModel()));
            }
            if (apiDefinition.getResponseMapper().getContentType() != null) {
                builder.responseType(apiDefinition.getResponseMapper().getContentType().getMimeType());
            }
            if (apiDefinition.getResponseMapper().getModel() != null) {
                builder.responseParams(ModelSpec.from(apiDefinition.getResponseMapper().getModel()));
            }
            return builder.build();
        }
    }

    @Value.Immutable
    @Value.Style(jdkOnly = true)
    @JsonSerialize(as = ImmutableSystemApisResponse.class)
    static abstract class SystemApisResponse {
        abstract List<ApiDefinitionResponse> getApis();
    }

    static void apiList(final ApiContext apiContext) {
        apiContext.newApiBuilder()
                .get("/system/apis", "系统api列表")
                .requiresResourceInjection()
                .requiresBusinessUnits()
                .handle(
                        RequestValidatorFactory.cleanUrlAndEmptyBodyValidator(),
                        (nonsense) -> {
                            final var apis = apiContext.getApis();
                            return Mono.just(ImmutableSystemApisResponse.builder()
                                    .apis(apis.stream()
                                            .filter(api -> !api.getPath().startsWith("/system"))
                                            .map(ApiDefinitionResponse::from)
                                            .collect(Collectors.toList()))
                                    .build());
                        },
                        ResponseMapperFactory.jsonResponseMapper200(SystemApisResponse.class));
    }

    public static void initialize(final ApiContext apiContext) {
        apiList(apiContext);
    }
}
