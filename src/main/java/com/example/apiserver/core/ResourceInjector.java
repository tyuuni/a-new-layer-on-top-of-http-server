package com.example.apiserver.core;

import io.javalin.http.Context;
import io.javalin.http.HttpCode;

import java.util.List;

/**
 * Resource Injector is just a practical name for interceptors because that's how they are mostly used. E.g.,
 *      1. We usually add watchdog timer to monitor the overall process time of requests, but in this case injected timestamp is not used by business logic.
 *      2. For tracing, we usually attach to the request a trace id which is used either implicitly (like it in java, this trace id can be stored in thread in blocking pattern,  or be lifted in non-blocking pattern),
 *         or explicitly (like it in nodejs, every logging should take the value of trace id).
 *      3. We usually have an authenticator to check if the request is from a valid user, which inject the user information into the context.
 * All meaningful interceptors must interact with the context and operates on the context, so calling them resource injectors is at least not misleading.
 * inject is actually equivalent to preHandle, I call it inject because in this framework we force every interceptor to inject a resource (that's why I call it resource injector).
 * the injected resource will be extracted exactly before business logic and later explicitly passed to business logic.
 * As for postHandle, I currently don't have a better name than that.
 * see {@link com.example.apiserver.core.ResourceInjectorBuilder} as well.
 */
public interface ResourceInjector<T extends InjectedResource> {
    T extract(Context context);

    /**
     * @return true if resource is injected successfully, the framework will continue to process the request.
     *         false if resource is not injected successfully. The injector has to throw an exception or write a response because the framework will stop further processing the request.
     */
    boolean inject(Context context);

    /**
     * most resource injectors do nothing after business logic.
     */
    default void postHandle(Context context) {}

    List<HttpParameter> getParameters();

    HttpCode getFailureCode();

    List<String> getFailureResponses();

    Class<T> getResourceClass();
}
