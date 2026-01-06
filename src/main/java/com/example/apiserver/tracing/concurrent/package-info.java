package com.example.apiserver.tracing.concurrent;

/**
 * 所有代码都是抄的https://github.com/opentracing-contrib/java-concurrent
 * 主要是替换Tracer为Tracing，因为opentracing中的span获取不到parentId
 * 至于为啥不直接用这个lib非得抄一个，因为必须要内嵌Logging追踪。
 * 其实可以外面再包一层，但实在是没必要。
 */
