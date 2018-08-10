
package com.android.example.teamwork.di;

import com.android.example.teamwork.ui.project.ProjectFragment;
import com.android.example.teamwork.ui.projectslist.ProjectListFragment;
import com.android.example.teamwork.ui.user.UserFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract ProjectFragment contributeRepoFragment();

    @ContributesAndroidInjector
    abstract UserFragment contributeUserFragment();

    @ContributesAndroidInjector
    abstract ProjectListFragment contributeSearchFragment();
}
