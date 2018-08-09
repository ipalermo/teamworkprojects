
package com.android.example.github.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.android.example.github.api.ApiResponse;
import com.android.example.github.api.GetProjectsResponse;
import com.android.example.github.api.TeamworkService;
import com.android.example.github.db.TeamworkDb;
import com.android.example.github.vo.GetProjectsResult;
import com.android.example.github.vo.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * A task that reads the getProjects result in the database and fetches the next page, if it has one.
 */
public class ProjectsNextPageTask implements Runnable {
    private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
    private final TeamworkService teamworkService;
    private final TeamworkDb db;

    ProjectsNextPageTask(TeamworkService teamworkService, TeamworkDb db) {
        this.teamworkService = teamworkService;
        this.db = db;
    }

    @Override
    public void run() {
        GetProjectsResult current = db.projectDao().findSearchResult();
        if(current == null) {
            liveData.postValue(null);
            return;
        }
        final Integer nextPage = current.next;
        if (nextPage == null) {
            liveData.postValue(Resource.success(false));
            return;
        }
        try {
            Response<GetProjectsResponse> response = teamworkService
                    .getProjects(nextPage).execute();
            ApiResponse<GetProjectsResponse> apiResponse = new ApiResponse<>(response);
            if (apiResponse.isSuccessful()) {
                // we merge all repo ids into 1 list so that it is easier to fetch the result list.
                List<Integer> ids = new ArrayList<>();
                ids.addAll(current.projectIds);
                //noinspection ConstantConditions
                ids.addAll(apiResponse.body.getProjectIds());
                GetProjectsResult merged = new GetProjectsResult(ids,
                        apiResponse.getNextPage());
                try {
                    db.beginTransaction();
                    db.projectDao().insert(merged);
                    db.projectDao().insertRepos(apiResponse.body.getProjects());
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                liveData.postValue(Resource.success(apiResponse.getNextPage() != null));
            } else {
                liveData.postValue(Resource.error(apiResponse.errorMessage, true));
            }
        } catch (IOException e) {
            liveData.postValue(Resource.error(e.getMessage(), true));
        }
    }

    LiveData<Resource<Boolean>> getLiveData() {
        return liveData;
    }
}
