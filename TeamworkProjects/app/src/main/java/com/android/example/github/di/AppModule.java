
package com.android.example.github.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.android.example.github.api.ServiceGenerator;
import com.android.example.github.api.TeamworkService;
import com.android.example.github.db.ProjectDao;
import com.android.example.github.db.TeamworkDb;
import com.android.example.github.db.UserDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ViewModelModule.class)
class AppModule {
    @Singleton @Provides
    TeamworkService provideGithubService() {
        return new ServiceGenerator()
                .createService(TeamworkService.class);
    }

    @Singleton @Provides
    TeamworkDb provideDb(Application app) {
        return Room.databaseBuilder(app, TeamworkDb.class,"teamwork.db").build();
    }

    @Singleton @Provides
    UserDao provideUserDao(TeamworkDb db) {
        return db.userDao();
    }

    @Singleton @Provides
    ProjectDao provideRepoDao(TeamworkDb db) {
        return db.projectDao();
    }
}
