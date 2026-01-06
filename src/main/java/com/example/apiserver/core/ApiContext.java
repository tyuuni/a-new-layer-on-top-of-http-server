package com.example.apiserver.core;

import brave.Tracing;
import io.javalin.Javalin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiContext {

    final Javalin app;
    final ApiBackbone apiBackbone;
    final Map<Class<? extends BusinessUnit>, BusinessUnit> unitByClass;
    final Map<Class<? extends InjectedResource>, ResourceInjectorBuilder<?>> injectorBuilderByResource;

    public ApiContext(final Javalin app,
                      final Tracing tracing) {
        this.app = app;
        this.apiBackbone = new ApiBackbone(app, tracing);
        this.unitByClass = new HashMap<>();
        this.injectorBuilderByResource = new HashMap<>();
    }

    public <T extends BusinessUnit> void registerUnit(final T unit) {
        final var clazz = unit.getClass();
        if (unitByClass.containsKey(clazz)) {
            throw new RuntimeException("a business class has at most 1 instance. class: " + clazz.getName());
        }
        unitByClass.put(clazz, unit);
    }

    public <T extends BusinessUnit> T registerByConstructingFundamentalUnit(final Class<T> unitClass,
                                                                            final Object... params)
            throws NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (unitByClass.containsKey(unitClass)) {
            throw new RuntimeException("a business class has at most 1 instance. class: " + unitClass.getName());
        }
        final Class<?>[] paramsTypes = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            paramsTypes[i] = params[i].getClass();
        }
        final var instance = unitClass.getDeclaredConstructor(paramsTypes).newInstance(params);
        unitByClass.put(unitClass, instance);
        return instance;
    }

    public <T extends BusinessUnit> T registerByConstructingCompositeUnit(final Class<T> unitClass,
                                                                          final Class<? extends BusinessUnit>... dependentUnits)
            throws NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (unitByClass.containsKey(unitClass)) {
            throw new RuntimeException("a business class has at most 1 instance. class: " + unitClass.getName());
        }
        final Object[] params = new BusinessUnit[dependentUnits.length];
        for (int i = 0; i < dependentUnits.length; i++) {
            final var unit = unitByClass.get(dependentUnits[i]);
            if (unit == null) {
                throw new RuntimeException(String.format("dependent class %s not registered for class %s", dependentUnits[i].getName(), unitClass.getName()));
            }
            params[i] = unit;
        }
        final var instance = unitClass.getDeclaredConstructor(dependentUnits).newInstance(params);
        unitByClass.put(unitClass, instance);
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T extends BusinessUnit> T getBusinessUnit(final Class<T> unitClass) {
        final var unit = (T) unitByClass.get(unitClass);
        if (unit == null) {
            throw new RuntimeException(String.format("business unit %s not registered", unitClass.getName()));
        }
        return unit;
    }

    public <T extends InjectedResource> void registerResourceInjector(final Class<T> clazz,
                                                                      final ResourceInjectorBuilder<T> builder) {
        injectorBuilderByResource.put(clazz, builder);
    }

    @SuppressWarnings("unchecked")
    public <T extends InjectedResource> ResourceInjectorBuilder<T> getResourceInjectorBuilder(final Class<T> clazz) {
        final var builder = (ResourceInjectorBuilder<T>) injectorBuilderByResource.get(clazz);
        if (builder == null) {
            throw new RuntimeException(String.format("resource injector builder %s not registered for class ", clazz.getName()));
        }
        return builder;
    }

    public ApiBuilder newApiBuilder() {
        return new ApiBuilder();
    }

    public void addGlobalInjector(final ResourceInjector<?> injector) {
        apiBackbone.addGlobalInjector(injector);
    }

    public void initializeFramework() {
        apiBackbone.initializeFramework();
        ApiBuilder.initialize(this);
    }

    public List<ApiDefinition<?, ?>> getApis() {
        return apiBackbone.getApis();
    }
}