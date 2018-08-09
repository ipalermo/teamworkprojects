
package com.android.example.github.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;
import android.util.SparseIntArray;

import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.GetProjectsResult;
import com.android.example.github.vo.Project;

import java.util.Collections;
import java.util.List;

/**
 * Interface for database access on Project related operations.
 */
@Dao
public abstract class ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Project... projects);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertContributors(List<Contributor> contributors);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertRepos(List<Project> repositories);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long createRepoIfNotExists(Project project);

    @Query("SELECT * FROM Project WHERE id = :id")
    public abstract LiveData<Project> load(int id);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT login, avatarUrl, projectId, contributions FROM contributor "
            + "WHERE projectId = :projectId "
            + "ORDER BY contributions DESC")
    public abstract LiveData<List<Contributor>> loadContributors(int projectId);

    @Query("SELECT * FROM Project "
            + "ORDER BY name DESC")
    public abstract LiveData<List<Project>> loadRepositories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(GetProjectsResult result);

    @Query("SELECT * FROM GetProjectsResult")
    public abstract LiveData<GetProjectsResult> search();

    public LiveData<List<Project>> loadOrdered(List<Integer> repoIds) {
        SparseIntArray order = new SparseIntArray();
        int index = 0;
        for (Integer repoId : repoIds) {
            order.put(repoId, index++);
        }
        return Transformations.map(loadById(repoIds), repositories -> {
            Collections.sort(repositories, (r1, r2) -> {
                int pos1 = order.get(r1.id);
                int pos2 = order.get(r2.id);
                return pos1 - pos2;
            });
            return repositories;
        });
    }

    @Query("SELECT * FROM Project WHERE id in (:projectIds)")
    protected abstract LiveData<List<Project>> loadById(List<Integer> projectIds);

    @Query("SELECT * FROM GetProjectsResult")
    public abstract GetProjectsResult findSearchResult();
}
