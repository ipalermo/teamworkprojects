package com.android.example.github.api;

import android.support.annotation.NonNull;

import com.android.example.github.vo.Repo;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GetProjectsResponse {
    @SerializedName("projects")
    private List<Repo> projects;
    private Integer nextPage;

    public List<Repo> getProjects() {
        return projects;
    }

    public void setProjects(List<Repo> projects) {
        this.projects = projects;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    @NonNull
    public List<Integer> getRepoIds() {
        List<Integer> repoIds = new ArrayList<>();
        for (Repo repo : projects) {
            repoIds.add(repo.id);
        }
        return repoIds;
    }
}
