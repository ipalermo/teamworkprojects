package com.android.example.github.api;

import android.support.annotation.NonNull;

import com.android.example.github.vo.Project;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GetProjectsResponse {
    @SerializedName("projects")
    private List<Project> projects;
    private Integer nextPage;

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    @NonNull
    public List<Integer> getProjectIds() {
        List<Integer> projectIds = new ArrayList<>();
        for (Project project : projects) {
            projectIds.add(project.id);
        }
        return projectIds;
    }
}
