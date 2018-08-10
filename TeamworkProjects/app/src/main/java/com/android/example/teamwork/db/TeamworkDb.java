
package com.android.example.teamwork.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.android.example.teamwork.vo.Contributor;
import com.android.example.teamwork.vo.GetProjectsResult;
import com.android.example.teamwork.vo.Project;
import com.android.example.teamwork.vo.User;

/**
 * Main database description.
 */
@Database(entities = {User.class, Project.class, Contributor.class,
        GetProjectsResult.class}, version = 3)
public abstract class TeamworkDb extends RoomDatabase {

    abstract public UserDao userDao();

    abstract public ProjectDao projectDao();
}
