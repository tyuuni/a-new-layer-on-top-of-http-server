package com.example.apiserver.unit.user;

import com.example.apiserver.core.ApiContext;
import com.example.apiserver.service.TemporaryMonolithicService;

public class UserUnitsRegister {

    public static void register(final ApiContext apiContext,
                                final TemporaryMonolithicService service)  {
        apiContext.registerUnit(new SingleUserByIdGetter(service));
        apiContext.registerUnit(new SingleUserByNfcCardIdGetter(service));
        apiContext.registerUnit(new UsersByTeacherIdGetter(service));
        apiContext.registerUnit(new SingleUserCreator(service));
        apiContext.registerUnit(new UsersByIdsDeleter(service));
        apiContext.registerUnit(new SingleUserUpdater(service));
    }
}
