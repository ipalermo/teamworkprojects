
package com.android.example.github.di;

import com.android.example.github.ui.repo.ProjectFragment;
import com.android.example.github.ui.search.ProjectListFragment;
import com.android.example.github.ui.user.UserFragment;

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
