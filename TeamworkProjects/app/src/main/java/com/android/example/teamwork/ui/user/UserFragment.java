
package com.android.example.teamwork.ui.user;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.teamwork.R;
import com.android.example.teamwork.binding.FragmentDataBindingComponent;
import com.android.example.teamwork.databinding.UserFragmentBinding;
import com.android.example.teamwork.di.Injectable;
import com.android.example.teamwork.ui.common.NavigationController;
import com.android.example.teamwork.ui.common.ProjectListAdapter;
import com.android.example.teamwork.util.AutoClearedValue;

import javax.inject.Inject;

public class UserFragment extends Fragment implements Injectable {
    private static final String LOGIN_KEY = "login";
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private UserViewModel userViewModel;
    @VisibleForTesting
    AutoClearedValue<UserFragmentBinding> binding;
    private AutoClearedValue<ProjectListAdapter> adapter;

    public static UserFragment create(String login) {
        UserFragment userFragment = new UserFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LOGIN_KEY, login);
        userFragment.setArguments(bundle);
        return userFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        UserFragmentBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.user_fragment,
                container, false, dataBindingComponent);
        dataBinding.setRetryCallback(() -> userViewModel.retry());
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userViewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.setLogin(getArguments().getString(LOGIN_KEY));
        userViewModel.getUser().observe(this, userResource -> {
            binding.get().setUser(userResource == null ? null : userResource.data);
            binding.get().setUserResource(userResource);
            // this is only necessary because espresso cannot read data binding callbacks.
            binding.get().executePendingBindings();
        });
        ProjectListAdapter rvAdapter = new ProjectListAdapter(dataBindingComponent,
                project -> navigationController.navigateToProject(project.id));
        binding.get().projectList.setAdapter(rvAdapter);
        this.adapter = new AutoClearedValue<>(this, rvAdapter);
        initRepoList();
    }

    private void initRepoList() {
        userViewModel.getRepositories().observe(this, repos -> {
            // no null checks for adapter.get() since LiveData guarantees that we'll not receive
            // the event if fragment is now show.
            if (repos == null) {
                adapter.get().replace(null);
            } else {
                adapter.get().replace(repos.data);
            }
        });
    }
}
