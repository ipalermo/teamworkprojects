
package com.android.example.teamwork.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.android.example.teamwork.api.ServiceGenerator;
import com.android.example.teamwork.api.TeamworkService;
import com.android.example.teamwork.db.ProjectDao;
import com.android.example.teamwork.db.TeamworkDb;
import com.android.example.teamwork.db.UserDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ViewModelModule.class)
class AppModule {
    @Singleton @Provides
    TeamworkService provideTeamworkService() {
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
