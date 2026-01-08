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
 *
 * As for postHandle, I currently don't have a better name than that.
 *
 * For ResourceInjector designing rules, I insist that resource injectors must be independent of each other.
 * Take authenticator and authorizer as an example:
 *   usually authenticator injects a user information into the context,
 *   and authorizer checks if the user has the permission to access the resource.
 * It seems very natural that authorizer should depend on authenticator.
 * If the interceptor chain is designed to be regarded as monolithic and indivisible,
 * it's kind of natural because all requests must go through all interceptors.
 * But actually for some interceptors, requests are just examined once and no actual work is done.
 * And we should also notice that authorization is usually complicated in most cases,
 * authorization through an interceptor doesn't often cover many cases.
 *
 * So from a modular perspective, we want all interceptors can be assembled in any applicable combination.
 * Then we should view authorizer as a combination of authenticator and an authorizer:
 * once we declare we want authorization, then authentication is done first.
 * So the dependency goes away, and the problem left to us is how to better organize codes.
 *
 * Here goes some examples to elaborate more the design goal of this framework.
 *
 * Usually, we have an authenticator like this:
 *      public class Authenticator extends HttpInterceptor {
 *          ...
 *
 *          public boolean preHandle(ServletRequest request) throws Exception {
 *              if (request.getRequestURI().startsWith("/public")) {
 *                  return true;
 *              }
 *              // do authentication..
 *          }
 *
 *          ...
 *      }
 * In this way, we actually introduce an implicit rule that all requests to "/public" endpoints bypass authentication.
 * There are at least 2 side effects:
 *      1. Beginner developers (for those who are not so curious about the framework) may just take as granted that all requests will have somehow a logged-in user in the context,
 *          because in a webapp, either most requests are publicly accessible, or most are privately accessible.
 *      2. One this "public" is written, we can hardly change it as the project grows.
 * However, in this new framework, if a developer wants to use authenticator, he/she must explicitly declare it and that makes he must read the corresponding code.
 *
 * Again, we usually might have a unified resource injector like this:
 *      public class MonolithicResourceInjector extends HttpInterceptor {
 *          ...
 *
 *          public boolean preHandle(ServletRequest request) {
 *              if (request.getRequestURI().startsWith("/user/")) {
 *                  final String userId = request.pathVariable(":userId");
 *                  // do injection
 *              }
 *              if (request.getRequestURI().startsWith("/course/")) {
 *                  final String courseId = request.pathVariable(":courseId");
 *                  // do injection
 *              }
 *              return true;
 *          }
 *
 *          ...
 *      }
 * This is worth because we fix both path and path variable here.
 * Besides, most requests actually has nothing to do with any resource injection.
 *
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
     * postHandle will be called anyway (if inject is called), even if inject returns false or throws an exception, or request handler throws an exception.
     * most resource injectors do nothing after business logic.
     */
    default void postHandle(Context context) {}

    List<HttpParameter> getParameters();

    List<ExampleResponse> getFailureResponses();

    Class<T> getResourceClass();
}
