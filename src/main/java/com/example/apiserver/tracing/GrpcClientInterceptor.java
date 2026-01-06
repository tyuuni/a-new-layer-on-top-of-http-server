package com.example.apiserver.tracing;

import brave.Span;
import brave.Tracing;
import brave.baggage.BaggagePropagation;
import brave.internal.Nullable;
import brave.propagation.Propagation;
import com.example.apiserver.ConfigConstants;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GrpcClientInterceptor implements ClientInterceptor {
    private final Tracing tracing;
    private final Propagation<String> propagation;
    private final Map<String, Metadata.Key<String>> keyByName;

    private static Map<String, Metadata.Key<String>> nameToKey(final Propagation<String> propagation) {
        final Map<String, Metadata.Key<String>> keyByName = new LinkedHashMap<>();
        for (final String keyName : propagation.keys()) {
            keyByName.put(keyName, Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER));
        }
        for (final String keyName : BaggagePropagation.allKeyNames(propagation)) {
            keyByName.put(keyName, Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER));
        }
        return Collections.unmodifiableMap(keyByName);
    }

    public GrpcClientInterceptor(final Tracing tracing) {
        this.tracing = tracing;
        this.propagation = tracing.propagation();
        this.keyByName = nameToKey(tracing.propagation());
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> method,
                                                               final CallOptions callOptions,
                                                               final Channel next) {
        final var span = tracing.tracer().nextSpan();
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(final Listener<RespT> responseListener, final Metadata headers) {
                final var injector = propagation.injector(new GrpcSetter(headers, keyByName));
                injector.inject(span.context(), headers);
                span.remoteServiceName(method.getServiceName());
                span.name(method.getFullMethodName());
                // TODO: add fields to span customizer
                span.start();
                final var clientCallListener = new ClientCallListener<RespT>(responseListener, tracing, span);
                try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
                    super.start(clientCallListener, headers);
                } catch (final Exception e) {
                    span.error(e).finish();
                    throw e;
                }
            }

            @Override
            public void sendMessage(ReqT message) {
                try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
                    if ("true".equals(ConfigConstants.BRAVE_PROPAGATION_DEBUG_FIELD.getValue(span.context()))) {
                        span.customizer().tag("grpc-request", message.toString());
                    }
                    super.sendMessage(message);
                }
            }

            @Override
            public void request(int numMessages) {
                try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
                    super.request(numMessages);
                }
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
                    delegate().cancel(message, cause);
                }
            }

            @Override
            public void halfClose() {
                try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
                    super.halfClose();
                } catch (final Exception e) {
                    span.error(e).finish();
                    throw e;
                }
            }
        };
    }

    private static class ClientCallListener<RespT> extends ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> {
        private final Tracing tracing;
        private final Span span;
        private final Metadata headers = new Metadata();

        ClientCallListener(final ClientCall.Listener<RespT> delegate,
                           final Tracing tracing,
                           final Span span) {
            super(delegate);
            this.tracing = tracing;
            this.span = span;
        }

        @Override
        public void onReady() {
            delegate().onReady();
        }

        // See instrumentation/RATIONALE.md for why the below response callbacks are invocation context
        @Override
        public void onHeaders(Metadata headers) {
            // onHeaders() JavaDoc mentions headers are not thread-safe, so we make a safe copy here.
            this.headers.merge(headers);
            try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
                delegate().onHeaders(headers);
            }
        }

        @Override
        public void onMessage(RespT message) {
            try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
//                if ("true".equals(ConfigConstants.BRAVE_PROPAGATION_DEBUG_FIELD.getValue(span.context()))) {
//                    span.customizer().tag("grpc-response", message.toString());
//                }
                delegate().onMessage(message);
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            try (final var scope = tracing.currentTraceContext().maybeScope(span.context())) {
                delegate().onClose(status, trailers);
            }
            span.finish();
        }
    }

    static class GrpcSetter implements Propagation.RemoteSetter<Metadata> {
        private final Map<String, Metadata.Key<String>> nameToKey;
        private final Metadata headers;

        GrpcSetter(final Metadata headers,
                   final Map<String, Metadata.Key<String>> nameToKey) {
            this.headers = headers;
            this.nameToKey = nameToKey;
        }

        @Override
        public Span.Kind spanKind() {
            return Span.Kind.CLIENT;
        }

        @Override
        public void put(final Metadata request,
                        final String fieldName,
                        final String value) {
            final Metadata.Key<String> key = this.nameToKey.get(fieldName);
            if (key == null) {
                return;
            }
            this.headers.removeAll(key);
            this.headers.put(key, value);
        }
    }
}
