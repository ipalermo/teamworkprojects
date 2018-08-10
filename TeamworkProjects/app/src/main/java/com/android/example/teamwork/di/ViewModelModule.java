package com.android.example.teamwork.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.android.example.teamwork.ui.project.ProjectViewModel;
import com.android.example.teamwork.ui.projectslist.ProjectsViewModel;
import com.android.example.teamwork.ui.user.UserViewModel;
import com.android.example.teamwork.viewmodel.GithubViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel.class)
    abstract ViewModel bindUserViewModel(UserViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ProjectsViewModel.class)
    abstract ViewModel bindSearchViewModel(ProjectsViewModel projectsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ProjectViewModel.class)
    abstract ViewModel bindRepoViewModel(ProjectViewModel projectViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(GithubViewModelFactory factory);
}
