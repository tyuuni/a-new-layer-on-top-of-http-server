package com.example.apiserver.core.reqres;

import com.example.apiserver.core.exception.CodeMessageErrorResponse;
import com.example.apiserver.core.exception.RequestValidationFailure;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.google.common.collect.ImmutableList;
import com.example.apiserver.core.ExampleResponse;
import com.example.apiserver.ErrorCodes;
import com.example.apiserver.util.JSONUtil;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.http.UploadedFile;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

// TODO: support JSR-380
public final class RequestValidatorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidatorFactory.class);

    private static final String DIRTY_URL = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.DIRTY_URL.value(),
                    "query string should be empty")
    );

    private static final String NO_CONTENT_TYPE_OR_CONTENT_EXPECTED = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.SHOULD_ELIMINATE_CONTENT_HEADER_AND_BODY.value(),
                    "expect empty body")
    );

    private static final String INVALID_CONTENT_TYPE = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.CONTENT_TYPE_NOT_MATCH.value(),
                    "content-type not acceptable")
    );

    private static final String DIRTY_BODY = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.DIRTY_BODY.value(),
                    "body not clean")
    );

    private static final String JSON_VALUE_TYPE_MISMATCH = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.JSON_VALUE_TYPE_MISMATCH.value(),
                    "incorrect value type")
    );

    private static final String JSON_TOO_LARGE = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.JSON_BODY_TOO_LARGE.value(),
                    "json too large")
    );

    private static final String FILE_PARAM_ERROR = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.IMAGE_PARAM_ERROR.value(),
                    "image param error")
    );
    private static final int DEFAULT_IMAGE_SIZE = 20 * 1024 * 1024;
    private static final String IMAGE_TOO_BIG = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.IMAGE_TOO_BIG.value(),
                    "image too big")
    );

    private static void validateNoQueryString(final Context context) {
        if (!(context.queryString() == null)) {
            throw new RequestValidationFailure("query string is not empty", DIRTY_URL);
        }
    }

    private static void validateEmptyBody(final Context context) {
        if (!(context.contentType() == null && context.contentLength() <= 0)) {
            throw new RequestValidationFailure("expect empty body", DIRTY_BODY);
        }
    }

    private static final List<ExampleResponse> CLEAN_URL_AND_EMPTY_BODY_FAILURE_RESPONSES = ImmutableList.of(
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, DIRTY_URL),
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, DIRTY_BODY)
    );

    private static final RequestValidator<Void> CLEAN_URL_AND_EMPTY_BODY_VALIDATOR = new RequestValidator<>() {
        @Override
        public Void validate(Context context) {
            validateNoQueryString(context);
            validateEmptyBody(context);
            return null;
        }

        @Override
        public ContentType getContentType() {
            return null;
        }

        @Override
        public Class<Void> getModel() {
            return Void.class;
        }

        @Override
        public List<ExampleResponse> getFailureResponses() {
            return CLEAN_URL_AND_EMPTY_BODY_FAILURE_RESPONSES;
        }
    };

    public static RequestValidator<Void> cleanUrlAndEmptyBodyValidator() {
        return CLEAN_URL_AND_EMPTY_BODY_VALIDATOR;
    }

    private final static int MAX_JSON_SIZE = 1024 * 1024;

    private static final List<ExampleResponse> CLEAN_JSON_VALIDATION_FAILURE_RESPONSES = ImmutableList.of(
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, DIRTY_URL),
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, INVALID_CONTENT_TYPE),
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, JSON_TOO_LARGE),
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, DIRTY_BODY),
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, JSON_VALUE_TYPE_MISMATCH)
    );

    public static <T> RequestValidator<T> buildCleanJsonValidator(final Class<T> clazz) {
        return new RequestValidator<T>() {

            @Override
            public T validate(Context context) {
                validateNoQueryString(context);
                if (!Objects.equals(context.contentType(), ContentType.APPLICATION_JSON.getMimeType())) {
                    LOGGER.warn("unexpected content type: {}", context.contentType());
                    throw new RequestValidationFailure("content type is not application/json", INVALID_CONTENT_TYPE);
                }
                if (context.contentLength() >= MAX_JSON_SIZE) {
                    LOGGER.warn("json body is too large: {}", context.contentLength());
                    throw new RequestValidationFailure("json body is too large", JSON_TOO_LARGE);
                }
                try {
                    return JSONUtil.readJsonAsNotSilent(context.body(), clazz);
                    // TODO: this implementation may not cover all cases.
                } catch (final UnrecognizedPropertyException | ValueInstantiationException e) {
                    LOGGER.warn("invalid json: {}", e.getMessage());
                    throw new RequestValidationFailure(e.getMessage(), DIRTY_BODY);
                } catch (final MismatchedInputException e) {
                    LOGGER.warn("invalid json: {}", e.getMessage());
                    throw new RequestValidationFailure(e.getMessage(), JSON_VALUE_TYPE_MISMATCH);
                } catch (final Exception e) {
                    LOGGER.warn("invalid json: {}", e.getMessage());
                    throw new RequestValidationFailure(e.getMessage(), DIRTY_BODY);
                }
            }

            @Override
            public ContentType getContentType() {
                return ContentType.APPLICATION_JSON;
            }



            @Override
            public Class<T> getModel() {
                return clazz;
            }

            @Override
            public List<ExampleResponse> getFailureResponses() {
                return CLEAN_JSON_VALIDATION_FAILURE_RESPONSES;
            }
        };
    }

    private static final List<ExampleResponse> EMPTY_BODY_QUERY_VALIDATION_FAILURE_RESPONSES = ImmutableList.of(
            ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, DIRTY_BODY)
    );

    public static <T> RequestValidator<T> buildQueryOnlyValidator(final Class<T> clazz,
                                                                  final String... allowedParams) {
        return new RequestValidator<>() {

            @Override
            public T validate(Context context) {
                validateEmptyBody(context);
                // TODO: here we should write linting rules to check if query params are in all camel case.
                // And we should not accept any form of array passing via query string.
                final var paramsMap = context.queryParamMap();
                if (paramsMap.size() != allowedParams.length) {
                    return null;
                }
                final Map<Object, Object> reconstructedMap = new HashMap<>();
                for (int i = 0; i < allowedParams.length; i++) {
                    final var value = paramsMap.get(allowedParams[i]);
                    if (value == null) {
                        return null;
                    }
                    reconstructedMap.put(allowedParams[i], value.get(0));
                }
                try {
                    return JSONUtil.readFromMap(reconstructedMap, clazz);
                } catch (final Exception e) {
                    return null;
                }
            }

            @Override
            public ContentType getContentType() {
                return null;
            }

            @Override
            public Class<T> getModel() {
                return clazz;
            }

            @Override
            public List<ExampleResponse> getFailureResponses() {
                return EMPTY_BODY_QUERY_VALIDATION_FAILURE_RESPONSES;
            }
        };
    }

    public static RequestValidator<UploadedFile> buildFormDataFileExtractor(final String field) {
        return new RequestValidator<>() {

            @Override
            public UploadedFile validate(Context context) {
                validateNoQueryString(context);
                if (!Objects.equals(context.contentType(), ContentType.MULTIPART_FORM_DATA.getMimeType())) {
                    LOGGER.warn("unexpected content type: {}", context.contentType());
                    throw new RequestValidationFailure("content type is not multipart/form-data", INVALID_CONTENT_TYPE);
                }
                final var file = context.uploadedFile(field);
                if (file == null) {
                    throw new RequestValidationFailure("file parameter is missing", FILE_PARAM_ERROR);
                }
                if (file.getSize() > DEFAULT_IMAGE_SIZE) {
                    throw new RequestValidationFailure("image file is too large", IMAGE_TOO_BIG);
                }
                return context.uploadedFile(field);
            }

            @Override
            public ContentType getContentType() {
                return ContentType.MULTIPART_FORM_DATA;
            }

            @Override
            public Class<UploadedFile> getModel() {
                return UploadedFile.class;
            }

            @Override
            public List<ExampleResponse> getFailureResponses() {
                return null;
            }
        };
    }
}
