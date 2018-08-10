
package com.android.example.teamwork.ui.user;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.android.example.teamwork.repository.ProjectRepository;
import com.android.example.teamwork.repository.UserRepository;
import com.android.example.teamwork.util.AbsentLiveData;
import com.android.example.teamwork.util.Objects;
import com.android.example.teamwork.vo.Project;
import com.android.example.teamwork.vo.Resource;
import com.android.example.teamwork.vo.User;

import java.util.List;

import javax.inject.Inject;

public class UserViewModel extends ViewModel {
    @VisibleForTesting
    final MutableLiveData<String> login = new MutableLiveData<>();
    private final LiveData<Resource<List<Project>>> repositories;
    private final LiveData<Resource<User>> user;
    @SuppressWarnings("unchecked")
    @Inject
    public UserViewModel(UserRepository userRepository, ProjectRepository projectRepository) {
        user = Transformations.switchMap(login, login -> {
            if (login == null) {
                return AbsentLiveData.create();
            } else {
                return userRepository.loadUser(login);
            }
        });
        repositories = Transformations.switchMap(login, login -> {
            if (login == null) {
                return AbsentLiveData.create();
            } else {
                return projectRepository.loadRepos(login);
            }
        });
    }

    @VisibleForTesting
    public void setLogin(String login) {
        if (Objects.equals(this.login.getValue(), login)) {
            return;
        }
        this.login.setValue(login);
    }

    @VisibleForTesting
    public LiveData<Resource<User>> getUser() {
        return user;
    }

    @VisibleForTesting
    public LiveData<Resource<List<Project>>> getRepositories() {
        return repositories;
    }

    @VisibleForTesting
    public void retry() {
        if (this.login.getValue() != null) {
            this.login.setValue(this.login.getValue());
        }
    }
}
