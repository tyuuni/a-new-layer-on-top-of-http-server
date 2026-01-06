package com.example.apiserver.unit.courseuserref;

import java.util.UUID;

class Utils {
    static String generateCourseUserRefId(final String studentId, final String courseId) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
