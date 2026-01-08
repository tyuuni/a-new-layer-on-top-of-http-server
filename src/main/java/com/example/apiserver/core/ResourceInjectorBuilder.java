package com.example.apiserver.core;

public interface ResourceInjectorBuilder<T extends InjectedResource> {
    /**
     * TODO: It might be better to define it as buildInjector(final Object config) to allow more flexible configuration.
     *       This doesn't break the overall design of this make-all-things-explicitly-defined-or-declared framework,
     *       because at the time one requires a injector, he/she surely knows what should be passed to it.
     * For now, this builder is defined in perspective of the parameter source.
     * @param clues see explanations above.
     * @return ResourceInjector
     */
    ResourceInjector<T> buildInjector(final String ...clues);
}
