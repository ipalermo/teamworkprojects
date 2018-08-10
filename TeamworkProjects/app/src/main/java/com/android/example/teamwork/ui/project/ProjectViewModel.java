
package com.android.example.teamwork.ui.project;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.android.example.teamwork.repository.ProjectRepository;
import com.android.example.teamwork.util.AbsentLiveData;
import com.android.example.teamwork.util.Objects;
import com.android.example.teamwork.vo.Contributor;
import com.android.example.teamwork.vo.Project;
import com.android.example.teamwork.vo.Resource;

import java.util.List;

import javax.inject.Inject;

public class ProjectViewModel extends ViewModel {
    @VisibleForTesting
    final MutableLiveData<Integer> projectId;
    private final LiveData<Resource<Project>> project;
    private final LiveData<Resource<List<Contributor>>> contributors;

    @Inject
    public ProjectViewModel(ProjectRepository repository) {
        this.projectId = new MutableLiveData<>();
        project = Transformations.switchMap(projectId, input -> {
            if (input == null) {
                return AbsentLiveData.create();
            }
            return repository.loadProject(input);
        });
        contributors = Transformations.switchMap(projectId, input -> {
            if (input == null) {
                return AbsentLiveData.create();
            } else {
                return repository.loadContributors(input);
            }

        });
    }

    public LiveData<Resource<Project>> getProject() {
        return project;
    }

    public LiveData<Resource<List<Contributor>>> getContributors() {
        return contributors;
    }

    public void retry() {
        Integer current = projectId.getValue();
        if (current != null) {
            projectId.setValue(current);
        }
    }

    @VisibleForTesting
    public void setId(Integer update) {
        if (Objects.equals(projectId.getValue(), update)) {
            return;
        }
        projectId.setValue(update);
    }
}
