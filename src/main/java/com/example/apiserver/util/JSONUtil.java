package com.example.apiserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.IOException;
import java.util.Map;

public final class JSONUtil {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        final SimpleModule module = new SimpleModule();
        OBJECT_MAPPER = JsonMapper.builder()
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
                .addModule(module)
                .build();
    }

    public static String writeAsJson(final Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readJsonAsNotSilent(final String json, final Class<T> clazz) throws JsonProcessingException {
        return OBJECT_MAPPER.readerFor(clazz).readValue(json);
    }

    public static <T> T readJsonAs(final String json, final Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readerFor(clazz).readValue(json);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readFromMap(final Map<Object, Object> keyValueMap, final Class<T> clazz) throws JsonProcessingException {
        return OBJECT_MAPPER.convertValue(keyValueMap, clazz);
    }

    private JSONUtil() {
    }
}
