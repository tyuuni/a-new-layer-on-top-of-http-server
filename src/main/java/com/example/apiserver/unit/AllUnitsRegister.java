package com.example.apiserver.unit;

import com.example.apiserver.core.ApiContext;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.unit.course.CourseUnitsRegister;
import com.example.apiserver.unit.course.CoursesByIdsDeleter;
import com.example.apiserver.unit.course.SingleCourseCreator;
import com.example.apiserver.unit.courseuserref.*;
import com.example.apiserver.unit.user.UserUnitsRegister;
import com.example.apiserver.unit.user.UsersByIdsDeleter;

import java.lang.reflect.InvocationTargetException;

public class AllUnitsRegister {

    public static void register(final ApiContext apiContext,
                         final TemporaryMonolithicService service) throws NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        UserUnitsRegister.register(apiContext, service);
        CourseUnitsRegister.register(apiContext, service);
        CourseUserRefsUnitRegister.register(apiContext, service);
        apiContext.registerUnit(new CompositeCourseCreator(
                apiContext.getBusinessUnit(SingleCourseCreator.class),
                apiContext.getBusinessUnit(CourseUserRefsCreator.class)));
        apiContext.registerUnit(new CompositeCourseDeleter(
                apiContext.getBusinessUnit(CoursesByIdsDeleter.class),
                apiContext.getBusinessUnit(CourseUserRefsByCourseIdsGetter.class),
                apiContext.getBusinessUnit(CourseUserRefsByIdsDeleter.class)));
        apiContext.registerUnit(new CompositeUsersDeleter(
                apiContext.getBusinessUnit(UsersByIdsDeleter.class),
                apiContext.getBusinessUnit(CourseUserRefsByStudentIdsGetter.class),
                apiContext.getBusinessUnit(CourseUserRefsByIdsDeleter.class)));
    }
}
