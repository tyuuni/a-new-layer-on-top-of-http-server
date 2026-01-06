package com.example.apiserver.unit.course;

import com.example.apiserver.core.ApiContext;
import com.example.apiserver.service.TemporaryMonolithicService;


public class CourseUnitsRegister {

    public static void register(final ApiContext apiContext,
                                final TemporaryMonolithicService service)  {
        apiContext.registerUnit(new SingleCourseByIdGetter(service));
        apiContext.registerUnit(new CoursesByIdsGetter(service));
        apiContext.registerUnit(new CoursesByTeacherIdGetter(service));
        apiContext.registerUnit(new SingleCourseCreator(service));
        apiContext.registerUnit(new CoursesByIdsDeleter(service));
    }
}
