
package com.android.example.teamwork.ui.common;

import android.support.v4.app.FragmentManager;

import com.android.example.teamwork.MainActivity;
import com.android.example.teamwork.R;
import com.android.example.teamwork.ui.project.ProjectFragment;
import com.android.example.teamwork.ui.projectslist.ProjectListFragment;
import com.android.example.teamwork.ui.user.UserFragment;

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
        ProjectListFragment projectListFragment = new ProjectListFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, projectListFragment)
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
