
package com.android.example.github.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.example.github.AppExecutors;
import com.android.example.github.api.ApiResponse;
import com.android.example.github.api.GetProjectsResponse;
import com.android.example.github.api.TeamworkService;
import com.android.example.github.db.ProjectDao;
import com.android.example.github.db.TeamworkDb;
import com.android.example.github.util.AbsentLiveData;
import com.android.example.github.util.RateLimiter;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.GetProjectsResult;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Repository that handles Project instances.
 *
 * Project - value object name
 * Repository - type of this class.
 */
@Singleton
public class ProjectRepository {

    private final TeamworkDb db;

    private final ProjectDao projectDao;

    private final TeamworkService teamworkService;

    private final AppExecutors appExecutors;

    private RateLimiter<String> projectListRateLimit = new RateLimiter<>(10, TimeUnit.MINUTES);

    @Inject
    public ProjectRepository(AppExecutors appExecutors, TeamworkDb db, ProjectDao projectDao,
                             TeamworkService teamworkService) {
        this.db = db;
        this.projectDao = projectDao;
        this.teamworkService = teamworkService;
        this.appExecutors = appExecutors;
    }

    public LiveData<Resource<List<Project>>> loadRepos(String owner) {
        return new NetworkBoundResource<List<Project>, List<Project>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Project> item) {
                projectDao.insertRepos(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Project> data) {
                return data == null || data.isEmpty() || projectListRateLimit.shouldFetch(owner);
            }

            @NonNull
            @Override
            protected LiveData<List<Project>> loadFromDb() {
                return projectDao.loadRepositories();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Project>>> createCall() {
                return teamworkService.getRepos(owner);
            }

            @Override
            protected void onFetchFailed() {
                projectListRateLimit.reset(owner);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Project>> loadProject(int id) {
        return new NetworkBoundResource<Project, Project>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull Project item) {
                projectDao.insert(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Project data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Project> loadFromDb() {
                return projectDao.load(id);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Project>> createCall() {
                return teamworkService.getProject(id);
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Contributor>>> loadContributors(int projectId) {
        return new NetworkBoundResource<List<Contributor>, List<Contributor>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Contributor> contributors) {
                for (Contributor contributor : contributors) {
                    contributor.setProjectId(projectId);
                }
                db.beginTransaction();
                try {
                    projectDao.createRepoIfNotExists(new Project(projectId,
                            "", "",
                            new Project.Company(null), false));
                    projectDao.insertContributors(contributors);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                Timber.d("rece saved contributors to db");
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Contributor> data) {
                Timber.d("rece contributor list from db: %s", data);
                return data == null || data.isEmpty();
            }

            @NonNull
            @Override
            protected LiveData<List<Contributor>> loadFromDb() {
                return projectDao.loadContributors(projectId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Contributor>>> createCall() {
                return teamworkService.getContributors(projectId);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> projectsNextPage() {
        ProjectsNextPageTask projectsNextPageTask = new ProjectsNextPageTask(
                teamworkService, db);
        appExecutors.networkIO().execute(projectsNextPageTask);
        return projectsNextPageTask.getLiveData();
    }

    public LiveData<Resource<List<Project>>> getProjects() {
        return new NetworkBoundResource<List<Project>, GetProjectsResponse>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull GetProjectsResponse item) {
                List<Integer> repoIds = item.getRepoIds();
                GetProjectsResult getProjectsResult = new GetProjectsResult(
                        repoIds, item.getNextPage());
                db.beginTransaction();
                try {
                    projectDao.insertRepos(item.getProjects());
                    projectDao.insert(getProjectsResult);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Project> data) {
                return data == null || data.isEmpty() || projectListRateLimit.shouldFetch("projects");
            }

            @NonNull
            @Override
            protected LiveData<List<Project>> loadFromDb() {
                return Transformations.switchMap(projectDao.search(), searchData -> {
                    if (searchData == null) {
                        return AbsentLiveData.create();
                    } else {
                        return projectDao.loadOrdered(searchData.projectIds);
                    }
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<GetProjectsResponse>> createCall() {
                return teamworkService.getProjects();
            }

            @Override
            protected GetProjectsResponse processResponse(ApiResponse<GetProjectsResponse> response) {
                GetProjectsResponse body = response.body;
                if (body != null) {
                    body.setNextPage(response.getNextPage());
                }
                return body;
            }
        }.asLiveData();
    }
}
