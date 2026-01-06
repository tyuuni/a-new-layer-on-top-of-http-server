package com.example.apiserver.unit.course;

import java.util.UUID;

class Utils {
    static String generateCourseDID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
