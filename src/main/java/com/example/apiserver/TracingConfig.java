package com.example.apiserver;

import brave.Tracing;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.opentracing.BraveTracer;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import com.example.apiserver.tracing.GrpcClientInterceptor;
import io.grpc.ClientInterceptor;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static com.example.apiserver.ConfigConstants.APP_NAME;

public final class TracingConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("tracing");

    @Bean
    Tracer tracer(@Autowired final Tracing tracing) {
        return BraveTracer.create(tracing);
    }

    @Bean
    ClientInterceptor grpcClientInterceptor(@Autowired final Tracing tracing) {
        return new GrpcClientInterceptor(tracing);
    }

    @Bean
    Tracing tracing(@Value(APP_NAME) final String serviceName) {
        return Tracing
                .newBuilder()
                .sampler(Sampler.ALWAYS_SAMPLE)
                .localServiceName(serviceName)
                .propagationFactory(BaggagePropagation
                        .newFactoryBuilder(B3Propagation.FACTORY)
                        .add(BaggagePropagationConfig.SingleBaggageField.remote(ConfigConstants.BRAVE_PROPAGATION_DEBUG_FIELD))
                        .build())
                .currentTraceContext(ThreadLocalCurrentTraceContext.create())
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(final TraceContext context,
                                       final MutableSpan span,
                                       final Cause cause) {
                        LOGGER.info(span.toString());
                        return true;
                    }
                })
                .build();
    }
}
