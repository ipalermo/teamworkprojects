
package com.android.example.github.ui.search;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.ProjectsListFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.ProjectListAdapter;
import com.android.example.github.util.AutoClearedValue;

import javax.inject.Inject;

public class ProjectListFragment extends Fragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    AutoClearedValue<ProjectsListFragmentBinding> binding;

    AutoClearedValue<ProjectListAdapter> adapter;

    private ProjectsViewModel projectsViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        ProjectsListFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.projects_list_fragment, container, false,
                        dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        projectsViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProjectsViewModel.class);
        initRecyclerView();
        ProjectListAdapter rvAdapter = new ProjectListAdapter(dataBindingComponent,
                project -> navigationController.navigateToProject(project.id));
        binding.get().projectList.setAdapter(rvAdapter);
        adapter = new AutoClearedValue<>(this, rvAdapter);
        projectsViewModel.loadProjects();
        binding.get().setCallback(() -> projectsViewModel.loadProjects());
    }

    private void initRecyclerView() {

        binding.get().projectList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();
                int lastPosition = layoutManager
                        .findLastVisibleItemPosition();
                if (lastPosition == adapter.get().getItemCount() - 1) {
                    projectsViewModel.loadNextPage();
                }
            }
        });
        projectsViewModel.getResults().observe(this, result -> {
            binding.get().setSearchResource(result);
            binding.get().setResultCount((result == null || result.data == null)
                    ? 0 : result.data.size());
            adapter.get().replace(result == null ? null : result.data);
            binding.get().executePendingBindings();
        });

        projectsViewModel.getLoadMoreStatus().observe(this, loadingMore -> {
            if (loadingMore == null) {
                binding.get().setLoadingMore(false);
            } else {
                binding.get().setLoadingMore(loadingMore.isRunning());
                String error = loadingMore.getErrorMessageIfNotHandled();
                if (error != null) {
                    Snackbar.make(binding.get().loadMoreBar, error, Snackbar.LENGTH_LONG).show();
                }
            }
            binding.get().executePendingBindings();
        });
    }
}
