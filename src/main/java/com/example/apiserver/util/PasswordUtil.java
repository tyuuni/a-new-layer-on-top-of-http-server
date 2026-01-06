package com.example.apiserver.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String encode(final String password) {
        return ENCODER.encode(password);
    }


    public static boolean match(final String raw,
                                final String encoded) {
        return ENCODER.matches(raw, encoded);
    }
}
