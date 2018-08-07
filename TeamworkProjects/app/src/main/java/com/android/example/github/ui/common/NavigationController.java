
package com.android.example.github.ui.common;

import android.support.v4.app.FragmentManager;

import com.android.example.github.MainActivity;
import com.android.example.github.R;
import com.android.example.github.ui.repo.ProjectFragment;
import com.android.example.github.ui.search.ProjectListFragment;
import com.android.example.github.ui.user.UserFragment;

import javax.inject.Inject;

/**
 * A utility class that handles navigation in {@link MainActivity}.
 */
public class NavigationController {
    private final int containerId;
    private final FragmentManager fragmentManager;
    @Inject
    public NavigationController(MainActivity mainActivity) {
        this.containerId = R.id.container;
        this.fragmentManager = mainActivity.getSupportFragmentManager();
    }

    public void navigateToProjectsList() {
        ProjectListFragment searchFragment = new ProjectListFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, searchFragment)
                .commitAllowingStateLoss();
    }

    public void navigateToProject(int id) {
        ProjectFragment fragment = ProjectFragment.create(id);
        String tag = "project" + "/" + id;
        fragmentManager.beginTransaction()
                .replace(containerId, fragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToUser(String login) {
        String tag = "user" + "/" + login;
        UserFragment userFragment = UserFragment.create(login);
        fragmentManager.beginTransaction()
                .replace(containerId, userFragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
