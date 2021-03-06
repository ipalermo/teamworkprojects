
package com.android.example.teamwork.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.example.teamwork.AppExecutors;
import com.android.example.teamwork.api.ApiResponse;
import com.android.example.teamwork.api.TeamworkService;
import com.android.example.teamwork.db.UserDao;
import com.android.example.teamwork.vo.Resource;
import com.android.example.teamwork.vo.User;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository that handles User objects.
 */
@Singleton
public class UserRepository {
    private final UserDao userDao;
    private final TeamworkService teamworkService;
    private final AppExecutors appExecutors;

    @Inject
    UserRepository(AppExecutors appExecutors, UserDao userDao, TeamworkService teamworkService) {
        this.userDao = userDao;
        this.teamworkService = teamworkService;
        this.appExecutors = appExecutors;
    }

    public LiveData<Resource<User>> loadUser(String login) {
        return new NetworkBoundResource<User,User>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull User item) {
                userDao.insert(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable User data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<User> loadFromDb() {
                return userDao.findByLogin(login);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<User>> createCall() {
                return teamworkService.getUser(login);
            }
        }.asLiveData();
    }
}
