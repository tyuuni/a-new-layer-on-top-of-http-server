package com.example.apiserver.store;

import com.example.apiserver.core.BusinessUnit;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.UUID;

public class TokenStore implements BusinessUnit {
    private final String prefix;
    private final StatefulRedisConnection<String, String> connection;
    private final int ttl;

    static String key(final String prefix,
                      final String id) {
        return prefix + id;
    }

    public TokenStore(final String prefix,
                      final StatefulRedisConnection<String, String> connection,
                      final int ttl) {
        this.prefix = prefix;
        this.connection = connection;
        this.ttl = ttl;
    }

    public String retrieveToken(final String userId) {
        return connection.sync().get(key(prefix, userId));
    }

    public String putToken(final String id) {
        final String token = UUID.randomUUID().toString().replace("-", "");
        connection.sync().setex(key(prefix, id), ttl, token);
        return token;
    }

    public void deleteToken(final String id) {
        connection.sync().del(key(prefix, id));
    }

    public int getDefaultTTL() {
        return ttl;
    }
}
