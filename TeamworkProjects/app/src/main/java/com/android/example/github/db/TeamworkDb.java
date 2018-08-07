
package com.android.example.github.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.GetProjectsResult;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.User;

/**
 * Main database description.
 */
@Database(entities = {User.class, Project.class, Contributor.class,
        GetProjectsResult.class}, version = 3)
public abstract class TeamworkDb extends RoomDatabase {

    abstract public UserDao userDao();

    abstract public ProjectDao projectDao();
}
