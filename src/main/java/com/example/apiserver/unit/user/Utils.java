package com.example.apiserver.unit.user;

import java.util.UUID;

class Utils {
    static String generateUserDID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
