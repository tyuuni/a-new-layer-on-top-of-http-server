package com.example.apiserver.tracing;

import brave.Span;
import io.lettuce.core.protocol.RedisCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public final class RedisSpanCustomizer implements BiConsumer<RedisCommand<Object, Object, Object>, Span> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSpanCustomizer.class);

    @Override
    public void accept(final RedisCommand<Object, Object, Object> command, final Span span) {
        if (command.getArgs() == null) {
            LOGGER.info("{}", command.getType().name());
        } else {
            LOGGER.info("{} {}", command.getType().name(), command.getOutput().toString());
        }
        span.customizer().tag("cmd", command.getType().name());
    }
}
