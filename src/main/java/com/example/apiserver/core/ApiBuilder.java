package com.example.apiserver.core;

import com.example.apiserver.core.reqres.RequestValidator;
import com.example.apiserver.core.reqres.ResponseMapper;
import io.javalin.http.HandlerType;
import kotlin.jvm.functions.*;
import reactor.core.publisher.Mono;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有变量写成static，一是为了写起来方便，二为了减少点overhead，导致的结果就是只能顺序执行
 */
@NotThreadSafe
public class ApiBuilder {
    static ApiContext apiContext;
    static HandlerType type;
    static String path;
    static String description;
    static List<ResourceInjector<? extends InjectedResource>> injectors;
    static List<BusinessUnit> units;

    static void initialize(final ApiContext ctx) {
        apiContext = ctx;
        injectors = new ArrayList<>(10);
        units = new ArrayList<>(10);
    }

    private static void renewApiContext(final HandlerType requestType,
                                        final String requestUrl,
                                        final String apiDescription) {
        type = requestType;
        path = requestUrl;
        description = apiDescription;
        injectors.clear();
        units.clear();
    }

    ApiBuilder() {
    }

    public ApiBuilderAtInjectorDeclaration get(final String url,
                                               final String apiDescription) {
        renewApiContext(HandlerType.GET, url, apiDescription);
        return new ApiBuilderAtInjectorDeclaration();
    }

    public ApiBuilderAtInjectorDeclaration put(final String url,
                                               final String apiDescription) {
        renewApiContext(HandlerType.PUT, url, apiDescription);
        return new ApiBuilderAtInjectorDeclaration();
    }

    public ApiBuilderAtInjectorDeclaration post(final String url,
                                                final String apiDescription) {
        renewApiContext(HandlerType.POST, url, apiDescription);
        return new ApiBuilderAtInjectorDeclaration();
    }

    public ApiBuilderAtInjectorDeclaration patch(final String url,
                                                 final String apiDescription) {
        renewApiContext(HandlerType.PATCH, url, apiDescription);
        return new ApiBuilderAtInjectorDeclaration();
    }

    public ApiBuilderAtInjectorDeclaration delete(final String url,
                                                  final String apiDescription) {
        renewApiContext(HandlerType.DELETE, url, apiDescription);
        return new ApiBuilderAtInjectorDeclaration();
    }

    public static class ApiBuilderAtInjectorDeclaration {
        public ApiBuilderRequiresInjectorN0 requiresResourceInjection() {
            return new ApiBuilderRequiresInjectorN0();
        }

        public <T extends InjectedResource> ApiBuilderRequiresInjectorN1<T> requiresResourceInjection(final Class<T> resourceClass,
                                                                                                      final String... clues) {
            final var injectorBuilder = apiContext.getResourceInjectorBuilder(resourceClass);
            if (injectorBuilder == null) {
                throw new RuntimeException("resource injector builder " + resourceClass.getName());
            }
            return new ApiBuilderRequiresInjectorN1<>(injectorBuilder.buildInjector(clues));
        }
    }

    public static class ApiBuilderRequiresInjectorN0 {

        public ApiBuilderRequiresInjectorN0UnitN0 requiresBusinessUnits() {
            return new ApiBuilderRequiresInjectorN0UnitN0();
        }

        public <B extends BusinessUnit> ApiBuilderRequiresInjectorN0UnitN1<B> requiresBusinessUnits(final Class<B> unitClass) {
            return new ApiBuilderRequiresInjectorN0UnitN1<>(unitClass);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit> ApiBuilderRequiresInjectorN0UnitN2<B1, B2> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                                                                   final Class<B2> unit2Class) {
            return new ApiBuilderRequiresInjectorN0UnitN2<>(unit1Class, unit2Class);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit> ApiBuilderRequiresInjectorN0UnitN3<B1, B2, B3> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                                                                                                final Class<B2> unit2Class,
                                                                                                                                                                final Class<B3> unit3Class) {
            return new ApiBuilderRequiresInjectorN0UnitN3<>(unit1Class, unit2Class, unit3Class);
        }
    }

    public static class ApiBuilderRequiresInjectorN0UnitN0 {
        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function1<R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN0UnitN1<B extends BusinessUnit> {

        ApiBuilderRequiresInjectorN0UnitN1(final Class<B> unitClass) {
            units.add(apiContext.getBusinessUnit(unitClass));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function2<B, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN0UnitN2<B1 extends BusinessUnit, B2 extends BusinessUnit> {

        ApiBuilderRequiresInjectorN0UnitN2(final Class<B1> unit1Class,
                                           final Class<B2> unit2Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function3<B1, B2, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN0UnitN3<B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit> {

        ApiBuilderRequiresInjectorN0UnitN3(final Class<B1> unit1Class,
                                           final Class<B2> unit2Class,
                                           final Class<B3> unit3Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
            units.add(apiContext.getBusinessUnit(unit3Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function4<B1, B2, B3, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN1<I extends InjectedResource> {

        ApiBuilderRequiresInjectorN1(final ResourceInjector<I> injector) {
            injectors.add(injector);
        }

        public <T extends InjectedResource> ApiBuilderRequiresInjectorN2<I, T> requiresResourceInjection(final Class<T> resourceClass,
                                                                                                         final String... clues) {
            final var injectorBuilder = apiContext.getResourceInjectorBuilder(resourceClass);
            if (injectorBuilder == null) {
                throw new RuntimeException("resource injector builder " + resourceClass.getName());
            }
            return new ApiBuilderRequiresInjectorN2<>(injectorBuilder.buildInjector(clues));
        }

        public ApiBuilderRequiresInjectorN1UnitsN0<I> requiresBusinessUnits() {
            return new ApiBuilderRequiresInjectorN1UnitsN0<>();
        }

        public <B extends BusinessUnit> ApiBuilderRequiresInjectorN1UnitsN1<I, B> requiresBusinessUnits(final Class<B> unitClass) {
            return new ApiBuilderRequiresInjectorN1UnitsN1<>(unitClass);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit>
        ApiBuilderRequiresInjectorN1UnitsN2<I, B1, B2> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                             final Class<B2> unit2Class) {
            return new ApiBuilderRequiresInjectorN1UnitsN2<>(unit1Class, unit2Class);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit>
        ApiBuilderRequiresInjectorN1UnitsN3<I, B1, B2, B3> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                 final Class<B2> unit2Class,
                                                                                 final Class<B3> unit3Class) {
            return new ApiBuilderRequiresInjectorN1UnitsN3<>(unit1Class, unit2Class, unit3Class);
        }
    }

    public static class ApiBuilderRequiresInjectorN1UnitsN0<I extends InjectedResource> {
        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function2<I, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN1UnitsN1<I extends InjectedResource, B extends BusinessUnit> {
        ApiBuilderRequiresInjectorN1UnitsN1(final Class<B> unitClass) {
            units.add(apiContext.getBusinessUnit(unitClass));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function3<I, B, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN1UnitsN2<I extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN1UnitsN2(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function4<I, B1, B2, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN1UnitsN3<I extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN1UnitsN3(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class,
                                            final Class<B3> unit3Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
            units.add(apiContext.getBusinessUnit(unit3Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function5<I, B1, B2, B3, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN2<I1 extends InjectedResource, I2 extends InjectedResource> {

        ApiBuilderRequiresInjectorN2(final ResourceInjector<I2> injector) {
            injectors.add(injector);
        }

        public <T extends InjectedResource> ApiBuilderRequiresInjectorN3<I1, I2, T> requiresResourceInjection(final Class<T> resourceClass,
                                                                                                              final String... clues) {
            final var injectorBuilder = apiContext.getResourceInjectorBuilder(resourceClass);
            if (injectorBuilder == null) {
                throw new RuntimeException("resource injector builder " + resourceClass.getName());
            }
            return new ApiBuilderRequiresInjectorN3<>(injectorBuilder.buildInjector(clues));
        }

        public ApiBuilderRequiresInjectorN2UnitsN0<I1, I2> requiresBusinessUnits() {
            return new ApiBuilderRequiresInjectorN2UnitsN0<>();
        }

        public <B extends BusinessUnit> ApiBuilderRequiresInjectorN2UnitsN1<I1, I2, B> requiresBusinessUnits(final Class<B> unitClass) {
            return new ApiBuilderRequiresInjectorN2UnitsN1<>(unitClass);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit>
        ApiBuilderRequiresInjectorN2UnitsN2<I1, I2, B1, B2> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                  final Class<B2> unit2Class) {
            return new ApiBuilderRequiresInjectorN2UnitsN2<>(unit1Class, unit2Class);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit>
        ApiBuilderRequiresInjectorN2UnitsN3<I1, I2, B1, B2, B3> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                      final Class<B2> unit2Class,
                                                                                      final Class<B3> unit3Class) {
            return new ApiBuilderRequiresInjectorN2UnitsN3<>(unit1Class, unit2Class, unit3Class);
        }
    }

    public static class ApiBuilderRequiresInjectorN2UnitsN0<I1 extends InjectedResource, I2 extends InjectedResource> {
        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function3<I1, I2, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN2UnitsN1<I1 extends InjectedResource, I2 extends InjectedResource, B extends BusinessUnit> {
        ApiBuilderRequiresInjectorN2UnitsN1(final Class<B> unitClass) {
            units.add(apiContext.getBusinessUnit(unitClass));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function4<I1, I2, B, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN2UnitsN2<I1 extends InjectedResource, I2 extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN2UnitsN2(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function5<I1, I2, B1, B2, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN2UnitsN3<I1 extends InjectedResource, I2 extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN2UnitsN3(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class,
                                            final Class<B3> unit3Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
            units.add(apiContext.getBusinessUnit(unit3Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function6<I1, I2, B1, B2, B3, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN3<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource> {

        ApiBuilderRequiresInjectorN3(final ResourceInjector<I3> injector) {
            injectors.add(injector);
        }

        public <T extends InjectedResource> ApiBuilderRequiresInjectorN4<I1, I2, I3, T> requiresResourceInjection(final Class<T> resourceClass,
                                                                                                                  final String... clues) {
            final var injectorBuilder = apiContext.getResourceInjectorBuilder(resourceClass);
            if (injectorBuilder == null) {
                throw new RuntimeException("resource injector builder " + resourceClass.getName());
            }
            return new ApiBuilderRequiresInjectorN4<>(injectorBuilder.buildInjector(clues));
        }

        public ApiBuilderRequiresInjectorN3UnitsN0<I1, I2, I3> requiresBusinessUnits() {
            return new ApiBuilderRequiresInjectorN3UnitsN0<>();
        }

        public <B extends BusinessUnit> ApiBuilderRequiresInjectorN3UnitsN1<I1, I2, I3, B> requiresBusinessUnits(final Class<B> unitClass) {
            return new ApiBuilderRequiresInjectorN3UnitsN1<>(unitClass);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit>
        ApiBuilderRequiresInjectorN3UnitsN2<I1, I2, I3, B1, B2> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                      final Class<B2> unit2Class) {
            return new ApiBuilderRequiresInjectorN3UnitsN2<>(unit1Class, unit2Class);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit>
        ApiBuilderRequiresInjectorN3UnitsN3<I1, I2, I3, B1, B2, B3> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                          final Class<B2> unit2Class,
                                                                                          final Class<B3> unit3Class) {
            return new ApiBuilderRequiresInjectorN3UnitsN3<>(unit1Class, unit2Class, unit3Class);
        }
    }

    public static class ApiBuilderRequiresInjectorN3UnitsN0<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource> {
        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function4<I1, I2, I3, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN3UnitsN1<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, B extends BusinessUnit> {
        ApiBuilderRequiresInjectorN3UnitsN1(final Class<B> unitClass) {
            units.add(apiContext.getBusinessUnit(unitClass));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function5<I1, I2, I3, B, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN3UnitsN2<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN3UnitsN2(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function6<I1, I2, I3, B1, B2, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN3UnitsN3<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN3UnitsN3(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class,
                                            final Class<B3> unit3Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
            units.add(apiContext.getBusinessUnit(unit3Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function7<I1, I2, I3, B1, B2, B3, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN4<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, I4 extends InjectedResource> {

        ApiBuilderRequiresInjectorN4(final ResourceInjector<I4> injector) {
            injectors.add(injector);
        }

        public ApiBuilderRequiresInjectorN4UnitsN0<I1, I2, I3, I4> requiresBusinessUnits() {
            return new ApiBuilderRequiresInjectorN4UnitsN0<>();
        }

        public <B extends BusinessUnit> ApiBuilderRequiresInjectorN4UnitsN1<I1, I2, I3, I4, B> requiresBusinessUnits(final Class<B> unitClass) {
            return new ApiBuilderRequiresInjectorN4UnitsN1<>(unitClass);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit>
        ApiBuilderRequiresInjectorN4UnitsN2<I1, I2, I3, I4, B1, B2> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                          final Class<B2> unit2Class) {
            return new ApiBuilderRequiresInjectorN4UnitsN2<>(unit1Class, unit2Class);
        }

        public <B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit>
        ApiBuilderRequiresInjectorN4UnitsN3<I1, I2, I3, I4, B1, B2, B3> requiresBusinessUnits(final Class<B1> unit1Class,
                                                                                              final Class<B2> unit2Class,
                                                                                              final Class<B3> unit3Class) {
            return new ApiBuilderRequiresInjectorN4UnitsN3<>(unit1Class, unit2Class, unit3Class);
        }
    }

    public static class ApiBuilderRequiresInjectorN4UnitsN0<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, I4 extends InjectedResource> {
        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function5<I1, I2, I3, I4, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN4UnitsN1<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, I4 extends InjectedResource, B extends BusinessUnit> {
        ApiBuilderRequiresInjectorN4UnitsN1(final Class<B> unitClass) {
            units.add(apiContext.getBusinessUnit(unitClass));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function6<I1, I2, I3, I4, B, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN4UnitsN2<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, I4 extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN4UnitsN2(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function7<I1, I2, I3, I4, B1, B2, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }

    public static class ApiBuilderRequiresInjectorN4UnitsN3<I1 extends InjectedResource, I2 extends InjectedResource, I3 extends InjectedResource, I4 extends InjectedResource, B1 extends BusinessUnit, B2 extends BusinessUnit, B3 extends BusinessUnit> {
        ApiBuilderRequiresInjectorN4UnitsN3(final Class<B1> unit1Class,
                                            final Class<B2> unit2Class,
                                            final Class<B3> unit3Class) {
            units.add(apiContext.getBusinessUnit(unit1Class));
            units.add(apiContext.getBusinessUnit(unit2Class));
            units.add(apiContext.getBusinessUnit(unit3Class));
        }

        public <R, T> ApiDefinition<R, T> handle(final RequestValidator<R> validator,
                                                 final Function8<I1, I2, I3, I4, B1, B2, B3, R, Mono<T>> handler,
                                                 final ResponseMapper<T> responseMapper) {
            return apiContext.apiBackbone.addApi(type, path, description, injectors, units, validator, GeneralizedHandler.generalize(handler), responseMapper);
        }
    }
}