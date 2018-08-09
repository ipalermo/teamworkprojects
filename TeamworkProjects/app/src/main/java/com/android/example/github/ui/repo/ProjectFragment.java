
package com.android.example.github.ui.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.ProjectFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.Resource;

import java.util.Collections;

import javax.inject.Inject;

/**
 * The UI Controller for displaying a Project's information with its contributors.
 */
public class ProjectFragment extends Fragment implements Injectable {

    private static final String PROJECT_ID = "project_id";

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ProjectViewModel projectViewModel;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<ProjectFragmentBinding> binding;
    AutoClearedValue<ContributorAdapter> adapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        projectViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProjectViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(PROJECT_ID)) {
            projectViewModel.setId(args.getInt(PROJECT_ID));
        } else {
            projectViewModel.setId(null);
        }
        LiveData<Resource<Project>> repo = projectViewModel.getProject();
        repo.observe(this, resource -> {
            binding.get().setProject(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
        });

        ContributorAdapter adapter = new ContributorAdapter(dataBindingComponent,
                contributor -> navigationController.navigateToUser(contributor.getLogin()));
        this.adapter = new AutoClearedValue<>(this, adapter);
//        binding.get().contributorList.setAdapter(adapter);
        initContributorList(projectViewModel);
    }

    private void initContributorList(ProjectViewModel viewModel) {
        viewModel.getContributors().observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null) {
                adapter.get().replace(listResource.data);
            } else {
                //noinspection ConstantConditions
                adapter.get().replace(Collections.emptyList());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        ProjectFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.project_fragment, container, false);
        dataBinding.setRetryCallback(() -> projectViewModel.retry());
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    public static ProjectFragment create(int id) {
        ProjectFragment projectFragment = new ProjectFragment();
        Bundle args = new Bundle();
        args.putInt(PROJECT_ID, id);
        projectFragment.setArguments(args);
        return projectFragment;
    }
}
