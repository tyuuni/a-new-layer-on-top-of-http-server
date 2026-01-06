package com.example.apiserver.unit.courseuserref;

import com.example.apiserver.core.ApiContext;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.unit.course.CoursesByIdsDeleter;

public class CourseUserRefsUnitRegister {
    public static void register(final ApiContext apiContext,
                                final TemporaryMonolithicService service) {
        apiContext.registerUnit(new CourseUserRefsByStudentIdsGetter(service));
        apiContext.registerUnit(new CourseUserRefsByCourseIdsGetter(service));
        apiContext.registerUnit(new CourseUserRefsCreator(service));
        apiContext.registerUnit(new CourseUserRefsByIdsDeleter(service));
    }
}
