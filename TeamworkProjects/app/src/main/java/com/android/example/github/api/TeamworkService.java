
package com.android.example.github.api;

import android.arch.lifecycle.LiveData;

import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * REST API access points
 */
public interface TeamworkService {
    @GET("users/{id}")
    LiveData<ApiResponse<User>> getUser(@Path("id") String login);

    @GET("users/{userId}/projects")
    LiveData<ApiResponse<List<Project>>> getProjects(@Path("userId") String login);

    @GET("projects/{id}.json")
    LiveData<ApiResponse<Project>> getProject(@Path("id") int id);

    @GET("projects/{projectId}.json?includePeople=true")
    LiveData<ApiResponse<List<Contributor>>> getContributors(@Path("projectId") int projectId);

    @GET("projects.json")
    LiveData<ApiResponse<GetProjectsResponse>> getProjects();

    @GET("projects.json")
    Call<GetProjectsResponse> getProjects(@Query("page") int page);
}
